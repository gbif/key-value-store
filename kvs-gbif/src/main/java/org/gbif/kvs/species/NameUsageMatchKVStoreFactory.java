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

import org.gbif.api.vocabulary.OccurrenceIssue;
import org.gbif.kvs.KeyValueStore;
import org.gbif.kvs.cache.KeyValueCache;
import org.gbif.kvs.conf.CachedHBaseKVStoreConfiguration;
import org.gbif.kvs.hbase.Command;
import org.gbif.kvs.hbase.HBaseStore;
import org.gbif.rest.client.configuration.ChecklistbankClientsConfiguration;
import org.gbif.rest.client.species.ChecklistbankService;
import org.gbif.rest.client.species.NameUsageMatch;
import org.gbif.rest.client.species.retrofit.ChecklistbankServiceSyncClient;

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
 * Factory of NameUsageMatch KV instances. When performing a match this can be configured to use taxonID and
 * scientificNameID first to attempt to locate a backbone concept using well managed checklists and taxon IDs before
 * making use of the names. The response is decorated with IUCN status before caching.
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

  public static KeyValueStore<Identification, NameUsageMatch> nameUsageMatchKVStore(CachedHBaseKVStoreConfiguration configuration,
                                                                                    ChecklistbankClientsConfiguration clientConfigurations,
                                                                                    IdMappingConfiguration idMappingConfig) throws IOException {
    ChecklistbankServiceSyncClient
      checklistbankServiceSyncClient = new ChecklistbankServiceSyncClient(clientConfigurations);
    Command closeHandler = closeHandler(checklistbankServiceSyncClient);

    KeyValueStore<Identification, NameUsageMatch> keyValueStore = Objects.nonNull(configuration.getHBaseKVStoreConfiguration())?
                                                                        hbaseKVStore(configuration, idMappingConfig,
                                                                                     checklistbankServiceSyncClient, closeHandler) : restKVStore(
      checklistbankServiceSyncClient, idMappingConfig, closeHandler);
    if (Objects.nonNull(configuration.getCacheCapacity())) {
      return KeyValueCache.cache(
          keyValueStore,
          configuration.getCacheCapacity(),
          Identification.class,
          NameUsageMatch.class,
          Optional.ofNullable(configuration.getCacheExpiryTimeInSeconds()).orElse(Long.MAX_VALUE));
    }
    return keyValueStore;
  }

  private static Command closeHandler(ChecklistbankServiceSyncClient client) {
    Command closeHandler = () -> {
      try {
        client.close();
      } catch (IOException ex) {
        throw logAndThrow(ex, "Error closing client");
      }
    };
    return closeHandler;
  }

  public static KeyValueStore<Identification, NameUsageMatch> nameUsageMatchKVStore(ChecklistbankClientsConfiguration clientConfigurations, IdMappingConfiguration idMappingConfig) {

    KeyValueStore<Identification, NameUsageMatch> keyValueStore = restKVStore(clientConfigurations, idMappingConfig);
    if (Objects.nonNull(clientConfigurations.getChecklistbankClientConfiguration().getFileCacheMaxSizeMb())) {
      return KeyValueCache.cache(keyValueStore, clientConfigurations.getChecklistbankClientConfiguration().getFileCacheMaxSizeMb(), Identification.class, NameUsageMatch.class);
    }
    return keyValueStore;
  }



  private static KeyValueStore<Identification, NameUsageMatch> hbaseKVStore(CachedHBaseKVStoreConfiguration configuration,
                                                                            IdMappingConfiguration idMappingConfig,
                                                                            ChecklistbankService checklistbankService,
                                                                            Command closeHandler) throws IOException {
    BackboneMatchByID backboneMatcher = new BackboneMatchByID(checklistbankService, idMappingConfig.getPrefixReplacement(), idMappingConfig.getPrefixToDataset());

    return HBaseStore.<Identification, NameUsageMatch, NameUsageMatch>builder()
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
                return matchAndDecorate(checklistbankService, identification, backboneMatcher);
              } catch (Exception ex) {
                throw logAndThrow(ex, "Error contacting the species math service");
              }
            })
         .withCloseHandler(closeHandler)
        .build();
  }


  /**
   * Matches the provided identification to a backbone concept, first using any well known taxon/name ID and then the name strings, and then
   * decorates the response with the IUCN status.
   */
  public static NameUsageMatch matchAndDecorate(ChecklistbankService checklistbankService, Identification identification, BackboneMatchByID backboneMatchByID) {
    NameUsageMatch nameOnlyLookup = checklistbankService.match(
        null, // No backbone key
        identification.getKingdom(),
        identification.getPhylum(),
        identification.getClazz(),
        identification.getOrder(),
        identification.getFamily(),
        identification.getGenus(),
        identification.getScientificName(),
        identification.getGenericName(),
        identification.getSpecificEpithet(),
        identification.getInfraspecificEpithet(),
        identification.getScientificNameAuthorship(),
        identification.getRank(),
        false,
        false);

    // Attempt to locate a backbone concept using the provided IDs
    Set<OccurrenceIssue> issues = new HashSet<>();
    Integer backboneKey = backboneMatchByID.lookupBackboneKey(identification, issues);
    if (backboneKey != null) {
      NameUsageMatch idLookup = checklistbankService.match(
          backboneKey, // names will be ignored if a good ID is provided
          identification.getKingdom(),
          identification.getPhylum(),
          identification.getClazz(),
          identification.getOrder(),
          identification.getFamily(),
          identification.getGenus(),
          identification.getScientificName(),
          identification.getGenericName(),
          identification.getSpecificEpithet(),
          identification.getInfraspecificEpithet(),
          identification.getScientificNameAuthorship(),
          identification.getRank(),
          false,
          false);

      idLookup.getIssues().addAll(issues);

      if (nameOnlyLookup.getUsage() != null &&
          idLookup.getUsage() != null &&
          nameOnlyLookup.getUsage().getKey()!=idLookup.getUsage().getKey()) {
        LOG.info("The backbone key found using IDs [{}] differs to that using names [{}]", idLookup.getUsage().getKey(),
            nameOnlyLookup.getUsage().getKey());
        idLookup.getIssues().add(OccurrenceIssue.TAXON_MATCH_NAME_AND_ID_AMBIGUOUS);
      }

      if (idLookup.getUsage() != null) {
        return IucnRedListCategoryDecorator.with(checklistbankService).decorate(idLookup);
      }
    }
    return IucnRedListCategoryDecorator.with(checklistbankService).decorate(nameOnlyLookup);
  }

  private static KeyValueStore<Identification, NameUsageMatch> restKVStore(ChecklistbankClientsConfiguration config, IdMappingConfiguration idMappingConfig) {
    ChecklistbankServiceSyncClient
      checklistbankServiceSyncClient = new ChecklistbankServiceSyncClient(config);

    return restKVStore(checklistbankServiceSyncClient, idMappingConfig, () -> {
      try {
        checklistbankServiceSyncClient.close();
      } catch (IOException ex) {
        throw logAndThrow(ex, "Error closing client");
      }
    });
  }

  /**
  * Builds a KV Store backed by the rest client.
  */
  private static KeyValueStore<Identification, NameUsageMatch> restKVStore(ChecklistbankService checklistbankService, IdMappingConfiguration idMappingConfig, Command closeHandler) {
    return new KeyValueStore<Identification, NameUsageMatch>() {

      BackboneMatchByID backboneMatcher = new BackboneMatchByID(checklistbankService, idMappingConfig.getPrefixReplacement(), idMappingConfig.getPrefixToDataset());

      @Override
      public NameUsageMatch get(Identification identification) {
        try {
          return matchAndDecorate(checklistbankService, identification, backboneMatcher);
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
