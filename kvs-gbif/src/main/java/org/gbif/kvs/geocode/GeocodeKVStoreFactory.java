/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.gbif.kvs.geocode;

import org.gbif.kvs.KeyValueStore;
import org.gbif.kvs.cache.KeyValueCache;
import org.gbif.kvs.conf.CachedHBaseKVStoreConfiguration;
import org.gbif.kvs.hbase.Command;
import org.gbif.kvs.hbase.HBaseStore;
import org.gbif.rest.client.configuration.ClientConfiguration;
import org.gbif.rest.client.geocode.GeocodeResponse;
import org.gbif.rest.client.geocode.GeocodeService;
import org.gbif.rest.client.geocode.Location;
import org.gbif.rest.client.geocode.retrofit.GeocodeServiceSyncClient;

import java.io.IOException;
import java.util.Objects;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;

import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.util.Bytes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;

/** Factory of Geocode KV instances. */
public class GeocodeKVStoreFactory {

  private static final Logger LOG = LoggerFactory.getLogger(GeocodeKVStoreFactory.class);

  // Used to store and retrieve JSON values stored in HBase
  private static final ObjectMapper MAPPER = new ObjectMapper();

  static {
    MAPPER.setSerializationInclusion(JsonInclude.Include.NON_NULL);
  }

  /** Hidden constructor. */
  private GeocodeKVStoreFactory() {
    // DO NOTHING
  }

  /**
   * Wraps an exception into a {@link RuntimeException}.
   * @param throwable to propagate
   * @param message to log and use for the exception wrapper
   * @return a new {@link RuntimeException}
   */
  private static RuntimeException logAndThrow(Throwable throwable, String message) {
    LOG.error(message, throwable);
    return new RuntimeException(throwable);
  }


  /**
   * Returns a function that maps HBase results into single String values.
   *
   * @param columnFamily HBase column in which values are stored
   * @param columnQualifier HBase column qualifier in which values are stored
   * @return a Result to GeocodeResponse mapping function
   */
  public static Function<Result, GeocodeResponse> resultMapper(byte[] columnFamily, byte[] columnQualifier) {
    return result ->  {
      try {
        byte[] value = result.getValue(columnFamily, columnQualifier);
        if(Objects.nonNull(value)) {
          return MAPPER.readValue(value, GeocodeResponse.class);
        }
        return null;
      } catch (Exception ex) {
        throw logAndThrow(ex, "Error reading value form HBase");
      }
    };
  }


  /**
   * Creates a mutator function that maps a key and a list of {@link Location} into a {@link
   * Put}.
   *
   * @param columnFamily HBase column in which values are stored
   * @param jsonColumnQualifier HBase column qualifier in which json responses are stored
   * @return a mapper from a key geocode responses into HBase Puts
   */
  public static BiFunction<byte[], GeocodeResponse, Put> valueMutator(byte[] columnFamily, byte[] jsonColumnQualifier) {
    return (key, geocodeResponses) -> {
      try {
        if (Objects.nonNull(geocodeResponses) && Objects.nonNull(geocodeResponses.getLocations())) {
          Put put = new Put(key);
          put.addColumn(columnFamily, jsonColumnQualifier, MAPPER.writeValueAsBytes(geocodeResponses));
          return put;
        }
        return null;
      } catch (IOException ex) {
        throw logAndThrow(ex, "Error serializing response into bytes");
      }
    };
  }

  /**
   * Create a new instance of a Geocode KV store/cache backed by an HBase table. This instance only
   * returns the ISO country code of the resulting geocode lookup.
   *
   * @param configuration KV store configuration
   * @param geocodeClientConfiguration Rest client configuration for the GeocodeService client
   * @return a new instance of Geocode KV store
   * @throws IOException if the Rest client can't be created
   */
  public static KeyValueStore<LatLng, GeocodeResponse> simpleGeocodeKVStore(CachedHBaseKVStoreConfiguration configuration,
                                                                            ClientConfiguration geocodeClientConfiguration) throws IOException {
    GeocodeServiceSyncClient geocodeService =  new GeocodeServiceSyncClient(geocodeClientConfiguration);
    return simpleGeocodeKVStore(configuration, geocodeService, () -> {
        try {
          geocodeService.close();
        } catch (IOException ex) {
          throw logAndThrow(ex, "Error closing client");
        }
    });

  }


  public static KeyValueStore<LatLng, GeocodeResponse> simpleGeocodeKVStore(CachedHBaseKVStoreConfiguration configuration,
                                                                            GeocodeService geocodeService,
                                                                            Command closeHandler) throws IOException {
    KeyValueStore<LatLng, GeocodeResponse> keyValueStore = Objects.nonNull(configuration.getHBaseKVStoreConfiguration())?
        hbaseKVStore(configuration, geocodeService, closeHandler) : restKVStore(geocodeService, closeHandler);

    if (Objects.nonNull(configuration.getCacheCapacity())) {
      return KeyValueCache.cache(keyValueStore, configuration.getCacheCapacity(), LatLng.class, GeocodeResponse.class,
          Optional.ofNullable(configuration.getCacheExpiryTimeInSeconds()).orElse(Long.MAX_VALUE));
    }
    return keyValueStore;

  }

  public static KeyValueStore<LatLng, GeocodeResponse> simpleGeocodeKVStore(ClientConfiguration clientConfiguration) {
    GeocodeServiceSyncClient geocodeService =  new GeocodeServiceSyncClient(clientConfiguration);
    KeyValueStore<LatLng, GeocodeResponse> keyValueStore = restKVStore(geocodeService, () -> {
      try {
        geocodeService.close();
      } catch (IOException ex) {
        throw logAndThrow(ex, "Error closing client");
      }
    });
    if (Objects.nonNull(clientConfiguration.getFileCacheMaxSizeMb())) {
      return KeyValueCache.cache(keyValueStore, clientConfiguration.getFileCacheMaxSizeMb(), LatLng.class, GeocodeResponse.class);
    }
    return keyValueStore;

  }

  public static KeyValueStore<LatLng, GeocodeResponse> simpleGeocodeKVStore(CachedHBaseKVStoreConfiguration configuration) throws IOException {
    KeyValueStore<LatLng, GeocodeResponse> keyValueStore = HBaseStore.<LatLng, GeocodeResponse, GeocodeResponse>builder()
        .withHBaseStoreConfiguration(configuration.getHBaseKVStoreConfiguration())
      .withLoaderRetryConfiguration(configuration.getLoaderRetryConfig())
        .withResultMapper(
            resultMapper(
                Bytes.toBytes(configuration.getHBaseKVStoreConfiguration().getColumnFamily()),
                Bytes.toBytes(configuration.getValueColumnQualifier())))
        .build();
    if (Objects.nonNull(configuration.getCacheCapacity())) {
      return KeyValueCache.cache(
          keyValueStore,
          configuration.getCacheCapacity(),
          LatLng.class,
          GeocodeResponse.class,
          Optional.ofNullable(configuration.getCacheExpiryTimeInSeconds()).orElse(Long.MAX_VALUE));
    }
    return keyValueStore;
  }

  /**
   * Builds a KVStore backed by Hbase.
   */
  private static KeyValueStore<LatLng, GeocodeResponse> hbaseKVStore(CachedHBaseKVStoreConfiguration configuration, GeocodeService geocodeService,
                                                                     Command closeHandler) throws IOException {
    return HBaseStore.<LatLng, GeocodeResponse, GeocodeResponse>builder()
        .withHBaseStoreConfiguration(configuration.getHBaseKVStoreConfiguration())
        .withLoaderRetryConfiguration(configuration.getLoaderRetryConfig())
        .withResultMapper(
            resultMapper(
                Bytes.toBytes(configuration.getHBaseKVStoreConfiguration().getColumnFamily()),
                Bytes.toBytes(configuration.getValueColumnQualifier())))
        .withValueMapper(Function.identity())
        .withValueMutator(
            valueMutator(
                Bytes.toBytes(configuration.getHBaseKVStoreConfiguration().getColumnFamily()),
                Bytes.toBytes(configuration.getValueColumnQualifier())))
        .withLoader(
            latLng -> {
              try {
                return new GeocodeResponse(geocodeService.reverse(latLng.getLatitude(), latLng.getLongitude()));
              } catch (Exception ex) {
                throw logAndThrow(ex, "Error contacting geocode service");
              }
            })
        .withCloseHandler(closeHandler)
        .build();
  }

  /**
   * Builds a KV Store backed by the rest client.
   */
  private static KeyValueStore<LatLng, GeocodeResponse> restKVStore(GeocodeService geocodeService, Command closeHandler) {
    return new KeyValueStore<LatLng, GeocodeResponse>() {


      @Override
      public GeocodeResponse get(LatLng key) {
        return new GeocodeResponse(geocodeService.reverse(key.getLatitude(), key.getLongitude()));
      }

      @Override
      public void close() throws IOException {
        closeHandler.execute();
      }
    };
  }
}
