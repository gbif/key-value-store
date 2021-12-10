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
package org.gbif.kvs.indexing.species;

import org.gbif.api.vocabulary.Rank;
import org.gbif.kvs.SaltedKeyGenerator;
import org.gbif.kvs.conf.CachedHBaseKVStoreConfiguration;
import org.gbif.kvs.indexing.options.ConfigurationMapper;
import org.gbif.kvs.species.IucnRedListCategoryDecorator;
import org.gbif.kvs.species.NameUsageMatchKVStoreFactory;
import org.gbif.kvs.species.SpeciesMatchRequest;
import org.gbif.kvs.species.TaxonParsers;
import org.gbif.rest.client.configuration.ChecklistbankClientsConfiguration;
import org.gbif.rest.client.configuration.ClientConfiguration;
import org.gbif.rest.client.species.ChecklistbankService;
import org.gbif.rest.client.species.NameUsageMatch;
import org.gbif.rest.client.species.retrofit.ChecklistbankServiceSyncClient;

import java.util.Optional;
import java.util.function.BiFunction;

import org.apache.beam.runners.spark.SparkRunner;
import org.apache.beam.sdk.Pipeline;
import org.apache.beam.sdk.PipelineResult;
import org.apache.beam.sdk.coders.AvroCoder;
import org.apache.beam.sdk.io.AvroIO;
import org.apache.beam.sdk.io.hbase.HBaseIO;
import org.apache.beam.sdk.options.PipelineOptionsFactory;
import org.apache.beam.sdk.transforms.Distinct;
import org.apache.beam.sdk.transforms.DoFn;
import org.apache.beam.sdk.transforms.ParDo;
import org.apache.beam.sdk.values.PCollection;
import org.apache.beam.sdk.values.TypeDescriptor;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.client.Mutation;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.util.Bytes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Apache Beam Pipeline that indexes Taxonomic NameUsage matches into an HBase KV table. */
public class NameUsageMatchIndexer {

  private static final Logger LOG = LoggerFactory.getLogger(NameUsageMatchIndexer.class);

  public static void main(String[] args) {
    NameUsageMatchIndexingOptions options =
        PipelineOptionsFactory.fromArgs(args).withValidation().as(NameUsageMatchIndexingOptions.class);
    run(options);
  }

  /**
   * Creates a {@link CachedHBaseKVStoreConfiguration} from a {@link NameUsageMatchIndexingOptions} instance.
   *
   * @param options pipeline options
   * @return a new instance of CachedHBaseKVStoreConfiguration
   */
  private static CachedHBaseKVStoreConfiguration nameUsageMatchKVConfiguration(NameUsageMatchIndexingOptions options) {
    return CachedHBaseKVStoreConfiguration.builder()
            .withHBaseKVStoreConfiguration(ConfigurationMapper.hbaseKVStoreConfiguration(options))
            .withValueColumnQualifier(options.getJsonColumnQualifier())
            .build();
  }

  /**
   * Creates a {@link ClientConfiguration} from a {@link NameUsageMatchIndexingOptions} instance for a Checklistbank client.
   *
   */
  public static ClientConfiguration clbClientConfiguration(NameUsageMatchIndexingOptions options) {
    return ClientConfiguration.builder()
      .withBaseApiUrl(options.getClbBaseApiUrl())
      .withTimeOut(options.getClbApiTimeOut())
      .withFileCacheMaxSizeMb(options.getClbRestClientCacheMaxSize())
      .build();
  }

  /**
   * Creates a {@link ClientConfiguration} from a {@link NameUsageMatchIndexingOptions} instance for a Checklistbank NameUsage client.
   *
   */
  public static ClientConfiguration nameUsageClientConfiguration(NameUsageMatchIndexingOptions options) {
    return ClientConfiguration.builder()
      .withBaseApiUrl(options.getNameUsageBaseApiUrl())
      .withTimeOut(options.getNameUsageApiTimeOut())
      .withFileCacheMaxSizeMb(options.getNameUsageRestClientCacheMaxSize())
      .build();
  }

  /**
   * Runs the indexing beam pipeline.
   * 1. Reads all latitude and longitude from the occurrence table.
   * 2. Selects only distinct coordinates
   * 3. Store the Geocode country lookup in table with the KV
   * format: latitude+longitude -> isoCountryCode2Digit.
   *
   * @param options beam HBase indexing options
   */
  private static void run(NameUsageMatchIndexingOptions options) {

    Pipeline pipeline = Pipeline.create(options);
    options.setRunner(SparkRunner.class);

    // Occurrence table to read
    String sourceGlob = options.getSourceGlob();

    // Config
    CachedHBaseKVStoreConfiguration storeConfiguration = nameUsageMatchKVConfiguration(options);
    ChecklistbankClientsConfiguration checklistbankClientsConfiguration = ChecklistbankClientsConfiguration.builder()
                                                                            .checklistbankClientConfiguration(clbClientConfiguration(options))
                                                                            .nameUSageClientConfiguration(nameUsageClientConfiguration(options))
                                                                            .build();

    Configuration hBaseConfiguration = storeConfiguration.getHBaseKVStoreConfiguration().hbaseConfig();

    // Read the occurrence table
    PCollection<SpeciesMatchRequest> inputRecords =
      pipeline.apply(AvroIO.parseGenericRecords(new AvroOccurrenceRecordToNameUsageRequest())
          .withCoder(AvroCoder.of(SpeciesMatchRequest.class))
          .from(sourceGlob)
      );

    // Select distinct coordinates
    PCollection<SpeciesMatchRequest> distinctNames =
        inputRecords
            .apply(
                Distinct.<SpeciesMatchRequest, String>withRepresentativeValueFn(SpeciesMatchRequest::getLogicalKey)
                    .withRepresentativeType(TypeDescriptor.of(String.class)));

    // Perform Geocode lookup
    distinctNames
        .apply(
            ParDo.of(
                new DoFn<SpeciesMatchRequest, Mutation>() {

                  private final SaltedKeyGenerator keyGenerator =
                      new SaltedKeyGenerator(
                          storeConfiguration.getHBaseKVStoreConfiguration().getNumOfKeyBuckets());

                  private transient ChecklistbankService checklistbankService;

                  private transient BiFunction<byte[], NameUsageMatch, Put> valueMutator;

                  @Setup
                  public void start() {
                    checklistbankService = new ChecklistbankServiceSyncClient(checklistbankClientsConfiguration);
                    valueMutator =
                        NameUsageMatchKVStoreFactory.valueMutator(
                            Bytes.toBytes(storeConfiguration.getHBaseKVStoreConfiguration().getColumnFamily()),
                            Bytes.toBytes(storeConfiguration.getValueColumnQualifier()));
                  }


                  @ProcessElement
                  public void processElement(ProcessContext context) {
                    try {
                      SpeciesMatchRequest request = context.element();
                      NameUsageMatch nameUsageMatch = IucnRedListCategoryDecorator.with(checklistbankService).decorate(checklistbankService.match(request.getKingdom(),
                                                                                                              request.getPhylum(),
                                                                                                              request.getClazz(),
                                                                                                              request.getOrder(),
                                                                                                              request.getFamily(),
                                                                                                              request.getGenus(),
                                                                                                              Optional.ofNullable(TaxonParsers.interpretRank(request)).map(Rank::name).orElse(null),
                                                                                                              TaxonParsers.interpretScientificName(request),
                                                                                                              false,
                                                                                                              false));

                      byte[] saltedKey = keyGenerator.computeKey(request.getLogicalKey());
                      context.output(valueMutator.apply(saltedKey, nameUsageMatch));

                    } catch (Exception ex) {
                      LOG.error("Error performing species match", ex);
                    }
                  }
                }))
        .apply(// Write to HBase
            HBaseIO.write()
                .withConfiguration(hBaseConfiguration)
                .withTableId(storeConfiguration.getHBaseKVStoreConfiguration().getTableName()));

    // Run and wait
    PipelineResult result = pipeline.run(options);
    result.waitUntilFinish();
  }
}
