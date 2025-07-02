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
package org.gbif.kvs.grscicoll;

import org.gbif.kvs.KeyValueStore;
import org.gbif.kvs.cache.KeyValueCache;
import org.gbif.kvs.conf.CachedHBaseKVStoreConfiguration;
import org.gbif.kvs.hbase.Command;
import org.gbif.kvs.hbase.HBaseStore;
import org.gbif.rest.client.RestClientFactory;
import org.gbif.rest.client.configuration.ClientConfiguration;
import org.gbif.rest.client.grscicoll.GrscicollLookupResponse;
import org.gbif.rest.client.grscicoll.GrscicollLookupService;

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

/** Factory of GrSciColl Lookup Service KV instances. */
public class GrscicollLookupKVStoreFactory {

  private static final Logger LOG = LoggerFactory.getLogger(GrscicollLookupKVStoreFactory.class);

  // Used to store and retrieve JSON values stored in HBase
  private static final ObjectMapper MAPPER = new ObjectMapper();

  static {
    MAPPER.setSerializationInclusion(JsonInclude.Include.NON_NULL);
  }

  /** Hidden constructor. */
  private GrscicollLookupKVStoreFactory() {
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
   * @return a Result to LookupResponse mapping function
   */
  public static Function<Result, GrscicollLookupResponse> resultMapper(byte[] columnFamily, byte[] columnQualifier) {
    return result ->  {
      try {
        byte[] value = result.getValue(columnFamily, columnQualifier);
        if(Objects.nonNull(value)) {
          return MAPPER.readValue(value, GrscicollLookupResponse.class);
        }
        return null;
      } catch (Exception ex) {
        throw logAndThrow(ex, "Error reading value form HBase");
      }
    };
  }


  /**
   * Creates a mutator function that maps a key and a {@link GrscicollLookupResponse} into a {@link
   * Put}.
   *
   * @param columnFamily HBase column in which values are stored
   * @param jsonColumnQualifier HBase column qualifier in which json responses are stored
   * @return a mapper from a key lookup responses into HBase Puts
   */
  public static BiFunction<byte[], GrscicollLookupResponse, Put> valueMutator(byte[] columnFamily, byte[] jsonColumnQualifier) {
    return (key, lookupResponse) -> {
      try {
        if (Objects.nonNull(lookupResponse)) {
          Put put = new Put(key);
          put.addColumn(columnFamily, jsonColumnQualifier, MAPPER.writeValueAsBytes(lookupResponse));
          return put;
        }
        return null;
      } catch (IOException ex) {
        throw logAndThrow(ex, "Error serializing response into bytes");
      }
    };
  }

  /**
   * Create a new instance of a GrSciColl Loopkup KV store/cache backed by an HBase table.
   *
   * @param configuration KV store configuration
   * @param grSciCollClientConfiguration Rest client configuration for the GrSciColl Lookup Service client
   * @return a new instance of GrSciColl Lookup Service KV store
   * @throws IOException if the Rest client can't be created
   */
  public static KeyValueStore<GrscicollLookupRequest, GrscicollLookupResponse> simpleGrscicollLookupKVStore(CachedHBaseKVStoreConfiguration configuration,
                                                                                                            ClientConfiguration grSciCollClientConfiguration) throws IOException {
    GrscicollLookupService lookupService =  RestClientFactory.createGrscicollLookupService(grSciCollClientConfiguration);
    return simpleGrscicollLookupKVStore(configuration, lookupService, () -> {});
  }


  public static KeyValueStore<GrscicollLookupRequest, GrscicollLookupResponse> simpleGrscicollLookupKVStore(CachedHBaseKVStoreConfiguration configuration,
                                                                            GrscicollLookupService lookupService,
                                                                            Command closeHandler) throws IOException {
    KeyValueStore<GrscicollLookupRequest, GrscicollLookupResponse> keyValueStore = Objects.nonNull(configuration.getHBaseKVStoreConfiguration())?
        hbaseKVStore(configuration, lookupService, closeHandler) : restKVStore(lookupService, closeHandler);

    if (Objects.nonNull(configuration.getCacheCapacity())) {
      return KeyValueCache.cache(
          keyValueStore,
          configuration.getCacheCapacity(),
          GrscicollLookupRequest.class,
          GrscicollLookupResponse.class,
          Optional.ofNullable(configuration.getCacheExpiryTimeInSeconds()).orElse(Long.MAX_VALUE));
    }
    return keyValueStore;
  }

  public static KeyValueStore<GrscicollLookupRequest, GrscicollLookupResponse> simpleGrscicollLookupKVStore(ClientConfiguration clientConfiguration) {
    GrscicollLookupService lookupService =  RestClientFactory.createGrscicollLookupService(clientConfiguration);
    KeyValueStore<GrscicollLookupRequest, GrscicollLookupResponse> keyValueStore = restKVStore(lookupService, () -> {});
    if (Objects.nonNull(clientConfiguration.getFileCacheMaxSizeMb())) {
      return KeyValueCache.cache(keyValueStore, clientConfiguration.getFileCacheMaxSizeMb(), GrscicollLookupRequest.class, GrscicollLookupResponse.class);
    }
    return keyValueStore;
  }

  public static KeyValueStore<GrscicollLookupRequest, GrscicollLookupResponse> simpleGrscicollLookupKVStore(CachedHBaseKVStoreConfiguration configuration) throws IOException {
    KeyValueStore<GrscicollLookupRequest, GrscicollLookupResponse> keyValueStore = HBaseStore.<GrscicollLookupRequest, GrscicollLookupResponse, GrscicollLookupResponse>builder()
        .withHBaseStoreConfiguration(configuration.getHBaseKVStoreConfiguration())
        .withLoaderRetryConfiguration(configuration.getLoaderRetryConfig())
        .withResultMapper(
            resultMapper(
                Bytes.toBytes(configuration.getHBaseKVStoreConfiguration().getColumnFamily()),
                Bytes.toBytes(configuration.getValueColumnQualifier()))
        )
        .build();
    if (Objects.nonNull(configuration.getCacheCapacity())) {
      return KeyValueCache.cache(
          keyValueStore,
          configuration.getCacheCapacity(),
          GrscicollLookupRequest.class,
          GrscicollLookupResponse.class,
          Optional.ofNullable(configuration.getCacheExpiryTimeInSeconds()).orElse(Long.MAX_VALUE));
    }
    return keyValueStore;
  }

  /**
   * Builds a KVStore backed by Hbase.
   */
  private static KeyValueStore<GrscicollLookupRequest, GrscicollLookupResponse> hbaseKVStore(CachedHBaseKVStoreConfiguration configuration, GrscicollLookupService lookupService,
                                                                     Command closeHandler) throws IOException {
    return HBaseStore.<GrscicollLookupRequest, GrscicollLookupResponse, GrscicollLookupResponse>builder()
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
            req -> {
              try {
                return lookupService.lookup(req);
              } catch (Exception ex) {
                throw logAndThrow(ex, "Error contacting lookup service");
              }
            })
        .withCloseHandler(closeHandler)
        .build();
  }

  /**
   * Builds a KV Store backed by the rest client.
   */
  private static KeyValueStore<GrscicollLookupRequest, GrscicollLookupResponse> restKVStore(GrscicollLookupService lookupService, Command closeHandler) {
    return new KeyValueStore<GrscicollLookupRequest, GrscicollLookupResponse>() {

      @Override
      public GrscicollLookupResponse get(GrscicollLookupRequest key) {
        return lookupService.lookup(key);
      }

      @Override
      public void close() throws IOException {
        closeHandler.execute();
      }
    };
  }
}
