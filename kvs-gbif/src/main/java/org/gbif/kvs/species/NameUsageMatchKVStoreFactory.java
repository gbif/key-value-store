package org.gbif.kvs.species;

import org.gbif.api.vocabulary.Rank;
import org.gbif.kvs.KeyValueStore;
import org.gbif.kvs.cache.KeyValueCache;
import org.gbif.kvs.conf.CachedHBaseKVStoreConfiguration;
import org.gbif.kvs.hbase.Command;
import org.gbif.kvs.hbase.HBaseStore;
import org.gbif.rest.client.configuration.ClientConfiguration;
import org.gbif.rest.client.species.NameMatchService;
import org.gbif.rest.client.species.NameUsageMatch;
import org.gbif.rest.client.species.retrofit.NameMatchServiceSyncClient;

import java.io.IOException;
import java.util.Objects;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;


import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.util.Bytes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/* Factory of NameUsageMatch KV instances. */
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
  public static Function<Result, NameUsageMatch> resultMapper(byte[] columnFamily, byte[] columnQualifier) {
    return result ->  {
        try {
           byte[] value = result.getValue(columnFamily, columnQualifier);
           if(Objects.nonNull(value)) {
            return MAPPER.readValue(value, NameUsageMatch.class);
           }
           return null;
        } catch (Exception ex) {
          throw logAndThrow(ex, "Error reading value form HBase");
        }
      };
  }


  /**
   * Creates a mutator function that maps a key and a list of {@link NameUsageMatch} into a {@link Put}.
   *
   * @param columnFamily HBase column in which values are stored
   * @param jsonColumnQualifier HBase column qualifier in which json responses are stored
   * @return a mapper from a key NameUsageMatch responses into HBase Puts
   */
  public static BiFunction<byte[], NameUsageMatch, Put> valueMutator(byte[] columnFamily, byte[] jsonColumnQualifier) {
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

  public static KeyValueStore<SpeciesMatchRequest, NameUsageMatch> nameUsageMatchKVStore(CachedHBaseKVStoreConfiguration configuration,
                                                                                         ClientConfiguration clientConfiguration) throws IOException {
    NameMatchServiceSyncClient nameMatchServiceSyncClient = new NameMatchServiceSyncClient(clientConfiguration);
    Command closeHandler = () -> {
      try {
        nameMatchServiceSyncClient.close();
      } catch (IOException ex) {
        throw logAndThrow(ex, "Error closing client");
      }
    };
    KeyValueStore<SpeciesMatchRequest, NameUsageMatch> keyValueStore = Objects.nonNull(configuration.getHBaseKVStoreConfiguration())?
                                                                        hbaseKVStore(configuration, nameMatchServiceSyncClient, closeHandler) : restKVStore(nameMatchServiceSyncClient, closeHandler);
    if (Objects.nonNull(configuration.getCacheCapacity())) {
      return KeyValueCache.cache(keyValueStore, configuration.getCacheCapacity(), SpeciesMatchRequest.class, NameUsageMatch.class);
    }
    return keyValueStore;
  }

  public static KeyValueStore<SpeciesMatchRequest, NameUsageMatch> nameUsageMatchKVStore(ClientConfiguration clientConfiguration) {
    KeyValueStore<SpeciesMatchRequest, NameUsageMatch> keyValueStore = restKVStore(clientConfiguration);
    if (Objects.nonNull(clientConfiguration.getFileCacheMaxSizeMb())) {
      return KeyValueCache.cache(keyValueStore, clientConfiguration.getFileCacheMaxSizeMb(), SpeciesMatchRequest.class, NameUsageMatch.class);
    }
    return keyValueStore;
  }



  private static KeyValueStore<SpeciesMatchRequest, NameUsageMatch> hbaseKVStore(CachedHBaseKVStoreConfiguration configuration,
                                                                                 NameMatchService nameMatchService,
                                                                                 Command closeHandler) throws IOException {
    return HBaseStore.<SpeciesMatchRequest, NameUsageMatch, NameUsageMatch>builder()
        .withHBaseStoreConfiguration(configuration.getHBaseKVStoreConfiguration())
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
            request -> {
              try {
                return nameMatchService.match(
                    request.getKingdom(),
                    request.getPhylum(),
                    request.getClazz(),
                    request.getOrder(),
                    request.getFamily(),
                    request.getGenus(),
                    Optional.ofNullable(TaxonParsers.interpretRank(request)).map(Rank::name).orElse(null),
                    TaxonParsers.interpretScientificName(request),
                    false,
                    false);
              } catch (Exception ex) {
                throw logAndThrow(ex, "Error contacting the species math service");
              }
            })
         .withCloseHandler(closeHandler)
        .build();
  }

  private static KeyValueStore<SpeciesMatchRequest, NameUsageMatch> restKVStore(ClientConfiguration clientConfiguration) {
    NameMatchServiceSyncClient nameMatchServiceSyncClient = new NameMatchServiceSyncClient(clientConfiguration);
    return restKVStore(nameMatchServiceSyncClient, () -> {
      try {
        nameMatchServiceSyncClient.close();
      } catch (IOException ex) {
        throw logAndThrow(ex, "Error closing client");
      }
    });
  }

  /**
  * Builds a KV Store backed by the rest client.
  */
  private static KeyValueStore<SpeciesMatchRequest, NameUsageMatch> restKVStore(NameMatchService nameMatchService, Command closeHandler) {
    return new KeyValueStore<SpeciesMatchRequest, NameUsageMatch>() {

      @Override
      public NameUsageMatch get(SpeciesMatchRequest key) {
        try {
          return nameMatchService.match(
              key.getKingdom(),
              key.getPhylum(),
              key.getClazz(),
              key.getOrder(),
              key.getFamily(),
              key.getGenus(),
              Optional.ofNullable(TaxonParsers.interpretRank(key)).map(Rank::name).orElse(null),
              TaxonParsers.interpretScientificName(key),
              false,
              false);
        } catch (Exception ex) {
          throw logAndThrow(ex, "Error contacting the species math service");
        }
      }

      @Override
      public void close() throws IOException {
        closeHandler.execute();
      }
    };
  }
}
