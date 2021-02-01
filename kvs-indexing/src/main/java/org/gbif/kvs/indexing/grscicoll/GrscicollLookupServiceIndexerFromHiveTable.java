package org.gbif.kvs.indexing.grscicoll;

import java.io.IOException;
import java.util.Collections;
import java.util.Objects;
import java.util.UUID;
import java.util.function.BiFunction;

import org.gbif.api.vocabulary.Country;
import org.gbif.kvs.SaltedKeyGenerator;
import org.gbif.kvs.conf.CachedHBaseKVStoreConfiguration;
import org.gbif.kvs.grscicoll.GrscicollLookupKVStoreFactory;
import org.gbif.kvs.grscicoll.GrscicollLookupRequest;
import org.gbif.kvs.indexing.options.ConfigurationMapper;
import org.gbif.rest.client.configuration.ClientConfiguration;
import org.gbif.rest.client.grscicoll.GrscicollLookupResponse;
import org.gbif.rest.client.grscicoll.GrscicollLookupService;
import org.gbif.rest.client.grscicoll.retrofit.GrscicollLookupServiceSyncClient;

import org.apache.beam.runners.spark.SparkRunner;
import org.apache.beam.sdk.Pipeline;
import org.apache.beam.sdk.PipelineResult;
import org.apache.beam.sdk.io.hbase.HBaseIO;
import org.apache.beam.sdk.io.hcatalog.HCatalogIO;
import org.apache.beam.sdk.options.PipelineOptionsFactory;
import org.apache.beam.sdk.transforms.DoFn;
import org.apache.beam.sdk.transforms.ParDo;
import org.apache.beam.sdk.values.PCollection;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.client.Mutation;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.util.Bytes;
import org.apache.hadoop.hive.conf.HiveConf;
import org.apache.hive.hcatalog.data.HCatRecord;
import org.apache.hive.hcatalog.data.schema.HCatSchema;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.gbif.kvs.indexing.grscicoll.HiveUtils.readSchema;

/** Apache Beam Pipeline that indexes GrSciColl lookup responses in a HBase KV table. */
public class GrscicollLookupServiceIndexerFromHiveTable {

  private static final Logger LOG =
      LoggerFactory.getLogger(GrscicollLookupServiceIndexerFromHiveTable.class);

  public static void main(String[] args) throws Exception {
    GrSciCollLookupIndexingOptions options =
        PipelineOptionsFactory.fromArgs(args)
            .withValidation()
            .as(GrSciCollLookupIndexingOptions.class);
    run(options);
  }

  /**
   * Creates a {@link CachedHBaseKVStoreConfiguration} from a {@link GrSciCollLookupIndexingOptions}
   * instance.
   *
   * @param options pipeline options
   * @return a new instance of CachedHBaseKVStoreConfiguration
   */
  private static CachedHBaseKVStoreConfiguration grSciCollKVStoreConfiguration(
      GrSciCollLookupIndexingOptions options) {
    return CachedHBaseKVStoreConfiguration.builder()
        .withHBaseKVStoreConfiguration(ConfigurationMapper.hbaseKVStoreConfiguration(options))
        .withValueColumnQualifier(options.getJsonColumnQualifier())
        .build();
  }

  /**
   * Runs the indexing beam pipeline. 1. Reads all collection and institution related fields from
   * the avro files. 2. Call the lookup WS using the fields previously read. 3. Store the WS
   * response in a HBase table with the KV format: {@link GrscicollLookupRequest#getLogicalKey()} ->
   * JSON response.
   *
   * @param options beam HBase indexing options
   */
  private static void run(GrSciCollLookupIndexingOptions options) throws Exception {

    Pipeline pipeline = Pipeline.create(options);
    options.setRunner(SparkRunner.class);

    // Config
    CachedHBaseKVStoreConfiguration storeConfiguration = grSciCollKVStoreConfiguration(options);
    ClientConfiguration grSciCollClientConfiguration =
        ConfigurationMapper.clientConfiguration(options);
    Configuration hBaseConfiguration =
        storeConfiguration.getHBaseKVStoreConfiguration().hbaseConfig();

    final HCatSchema schema = readSchema(options);

    PCollection<HCatRecord> records =
        pipeline.apply(
            HCatalogIO.read()
                .withConfigProperties(
                    Collections.singletonMap(
                        HiveConf.ConfVars.METASTOREURIS.varname, options.getMetastoreUris()))
                .withDatabase(options.getDatabase())
                .withTable(options.getTable()));

    // Perform GRSciColl lookup
    records
        .apply(
            ParDo.of(
                new DoFn<HCatRecord, Mutation>() {

                  private final SaltedKeyGenerator keyGenerator =
                      new SaltedKeyGenerator(
                          storeConfiguration.getHBaseKVStoreConfiguration().getNumOfKeyBuckets());

                  private transient GrscicollLookupService lookupService;

                  private transient BiFunction<byte[], GrscicollLookupResponse, Put> valueMutator;

                  @Setup
                  public void start() {
                    lookupService =
                        new GrscicollLookupServiceSyncClient(grSciCollClientConfiguration);
                    valueMutator =
                        GrscicollLookupKVStoreFactory.valueMutator(
                            Bytes.toBytes(
                                storeConfiguration
                                    .getHBaseKVStoreConfiguration()
                                    .getColumnFamily()),
                            Bytes.toBytes(storeConfiguration.getValueColumnQualifier()));
                  }

                  @ProcessElement
                  public void processElement(ProcessContext context) {
                    try {
                      HCatRecord record = context.element();

                      GrscicollLookupRequest req =
                          HiveUtils.convertToGrSciCollRequest(record, schema);

                      GrscicollLookupResponse lookupResponse =
                          lookupService.lookup(
                              req.getInstitutionCode(),
                              req.getOwnerInstitutionCode(),
                              req.getInstitutionId(),
                              req.getCollectionCode(),
                              req.getCollectionId(),
                              req.getDatasetKey() != null
                                  ? UUID.fromString(req.getDatasetKey())
                                  : null,
                              req.getCountry() != null
                                  ? Country.fromIsoCode(req.getCountry())
                                  : null);
                      if (Objects.nonNull(lookupResponse)) {
                        byte[] saltedKey = keyGenerator.computeKey(req.getLogicalKey());
                        context.output(valueMutator.apply(saltedKey, lookupResponse));
                      }
                    } catch (Exception ex) {
                      LOG.error("Error performing Geocode lookup", ex);
                    }
                  }

                  @Teardown
                  public void tearDown() {
                    if (Objects.nonNull(lookupService)) {
                      try {
                        lookupService.close();
                      } catch (IOException ex) {
                        LOG.error("Error closing lookup service", ex);
                      }
                    }
                  }
                }))
        .apply( // Write to HBase
            HBaseIO.write()
                .withConfiguration(hBaseConfiguration)
                .withTableId(storeConfiguration.getHBaseKVStoreConfiguration().getTableName()));

    // Run and wait
    PipelineResult result = pipeline.run(options);
    result.waitUntilFinish();
  }
}
