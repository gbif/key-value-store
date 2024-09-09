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
package org.gbif.kvs.species;
import org.gbif.kvs.KeyValueStore;
import org.gbif.kvs.cache.KeyValueCache;
import org.gbif.kvs.conf.CachedHBaseKVStoreConfiguration;
import org.gbif.kvs.hbase.Command;
import org.gbif.kvs.hbase.HBaseStore;
import org.gbif.rest.client.RestClientFactory;
import org.gbif.rest.client.configuration.ClientConfiguration;
import org.gbif.rest.client.species.NameUsageMatchResponse;
import org.gbif.rest.client.species.NameUsageMatchingService;

import java.io.IOException;
import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Function;

import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.util.Bytes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Factory of NameUsageMatch KV instances.
*/
public class NameUsageMatchKVStoreFactory {

  private static final Logger LOG = LoggerFactory.getLogger(NameUsageMatchKVStoreFactory.class);

  // Used to store and retrieve JSON values stored in HBase
  private static final ObjectMapper MAPPER = new ObjectMapper();

  static {
    MAPPER.setSerializationInclusion(JsonInclude.Include.NON_NULL);
  }

  /** Hidden constructor. */
  private NameUsageMatchKVStoreFactory() {
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
   * Returns a function that maps HBase results into NameUsageMatch values.
   *
   * @param columnFamily HBase column in which values are stored
   * @param columnQualifier HBase column qualifier in which values are stored
   * @return a Result to NameUsageMatch mapping function
   */
  public static Function<Result, NameUsageMatchResponse> resultMapper(byte[] columnFamily, byte[] columnQualifier) {
    return result ->  {
        try {
           byte[] value = result.getValue(columnFamily, columnQualifier);
           if (Objects.nonNull(value)) {
            return MAPPER.readValue(value, NameUsageMatchResponse.class);
           }
           return null;
        } catch (Exception ex) {
          throw logAndThrow(ex, "Error reading value form HBase");
        }
      };
  }

  /**
   * Creates a mutator function that maps a key and a list of {@link NameUsageMatchResponse} into a {@link Put}.
   *
   * @param columnFamily HBase column in which values are stored
   * @param jsonColumnQualifier HBase column qualifier in which json responses are stored
   * @return a mapper from a key NameUsageMatch responses into HBase Puts
   */
  public static BiFunction<byte[], NameUsageMatchResponse, Put> valueMutator(byte[] columnFamily, byte[] jsonColumnQualifier) {
    return (key, nameUsageMatch) -> {
      try {
        if (Objects.nonNull(nameUsageMatch) ) {
          Put put = new Put(key);
          put.addColumn(columnFamily, jsonColumnQualifier, MAPPER.writeValueAsBytes(nameUsageMatch));
          return put;
        }
        return null;
      } catch (IOException ex) {
        throw logAndThrow(ex, "Error serializing response into bytes");
      }
    };
  }

  /**
   * Creates a KV Store that uses the provided configuration to connect to the species match service.
   *
   * @param configuration to use to connect to the species match service
   * @return a new KV Store
   * @throws IOException if the KV Store cannot be created
   */
  public static KeyValueStore<NameUsageMatchRequest, NameUsageMatchResponse> nameUsageMatchKVStore(CachedHBaseKVStoreConfiguration configuration,
                                                                                                   ClientConfiguration clientConfiguration) throws IOException {
    NameUsageMatchingService
            nameUsageMatchService = RestClientFactory.createNameMatchService(clientConfiguration);

    KeyValueStore<NameUsageMatchRequest, NameUsageMatchResponse> keyValueStore = Objects.nonNull(configuration.getHBaseKVStoreConfiguration()) ?
                                                                        hbaseKVStore(configuration, nameUsageMatchService, () -> {}) : restKVStore(
            nameUsageMatchService, () -> {});
    if (Objects.nonNull(configuration.getCacheCapacity())) {
      return KeyValueCache.cache(
          keyValueStore,
          configuration.getCacheCapacity(),
          NameUsageMatchRequest.class,
          NameUsageMatchResponse.class,
          Optional.ofNullable(configuration.getCacheExpiryTimeInSeconds()).orElse(Long.MAX_VALUE));
    }
    return keyValueStore;
  }

  public static KeyValueStore<NameUsageMatchRequest, NameUsageMatchResponse> nameUsageMatchKVStore(ClientConfiguration clientConfiguration) {
    KeyValueStore<NameUsageMatchRequest, NameUsageMatchResponse> keyValueStore = restKVStore(RestClientFactory.createNameMatchService(clientConfiguration), () -> {});
    if (Objects.nonNull(clientConfiguration.getFileCacheMaxSizeMb())) {
      return KeyValueCache.cache(keyValueStore, clientConfiguration.getFileCacheMaxSizeMb(), NameUsageMatchRequest.class, NameUsageMatchResponse.class);
    }
    return keyValueStore;
  }


  private static KeyValueStore<NameUsageMatchRequest, NameUsageMatchResponse> hbaseKVStore(CachedHBaseKVStoreConfiguration configuration,
                                                                                           NameUsageMatchingService nameUsageMatchService,
                                                                                           Command closeHandler) throws IOException {

    return HBaseStore.<NameUsageMatchRequest, NameUsageMatchResponse, NameUsageMatchResponse>builder()
        .withHBaseStoreConfiguration(configuration.getHBaseKVStoreConfiguration())
        .withLoaderRetryConfiguration(configuration.getLoaderRetryConfig())
        .withResultMapper(
            resultMapper(
                Bytes.toBytes(
                    configuration.getHBaseKVStoreConfiguration().getColumnFamily()),
                Bytes.toBytes(configuration.getValueColumnQualifier())))
        .withValueMapper(Function.identity())
        .withValueMutator(
            valueMutator(
                Bytes.toBytes(configuration.getHBaseKVStoreConfiguration().getColumnFamily()),
                Bytes.toBytes(configuration.getValueColumnQualifier())
            ))
        .withLoader(
            identification -> {
              try {
                return nameUsageMatchService.match(identification);
              } catch (Exception ex) {
                throw logAndThrow(ex, "Error contacting the species match service");
              }
            })
         .withCloseHandler(closeHandler)
        .build();
  }

  /**
  * Builds a KV Store backed by the rest client.
  */
  private static KeyValueStore<NameUsageMatchRequest, NameUsageMatchResponse> restKVStore(final NameUsageMatchingService nameUsageMatchService,
                                                                                          Command closeHandler) {
    return new KeyValueStore<NameUsageMatchRequest, NameUsageMatchResponse>() {

      @Override
      public NameUsageMatchResponse get(NameUsageMatchRequest identification) {
        try {
          return nameUsageMatchService.match(identification);
        } catch (Exception ex) {
          throw logAndThrow(ex, "Error contacting the species math service");
        }
      }

      @Override
      public void close() {
        closeHandler.execute();
      }
    };
  }
}
