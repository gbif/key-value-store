package org.gbif.kvs.indexing.grscicoll;

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
import org.gbif.utils.file.properties.PropertiesUtil;

import java.io.IOException;
import java.util.Objects;
import java.util.Properties;
import java.util.UUID;
import java.util.function.BiFunction;

import org.apache.beam.runners.spark.SparkRunner;
import org.apache.beam.sdk.Pipeline;
import org.apache.beam.sdk.PipelineResult;
import org.apache.beam.sdk.coders.KvCoder;
import org.apache.beam.sdk.coders.SerializableCoder;
import org.apache.beam.sdk.coders.StringUtf8Coder;
import org.apache.beam.sdk.extensions.joinlibrary.Join;
import org.apache.beam.sdk.io.hbase.HBaseIO;
import org.apache.beam.sdk.io.jdbc.JdbcIO;
import org.apache.beam.sdk.options.PipelineOptionsFactory;
import org.apache.beam.sdk.transforms.DoFn;
import org.apache.beam.sdk.transforms.Keys;
import org.apache.beam.sdk.transforms.ParDo;
import org.apache.beam.sdk.transforms.WithKeys;
import org.apache.beam.sdk.values.KV;
import org.apache.beam.sdk.values.PCollection;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.client.Delete;
import org.apache.hadoop.hbase.client.Mutation;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.Scan;
import org.apache.hadoop.hbase.util.Bytes;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class GrscicollPipelineWorkflow {

  private static final String NULL_VALUE_IN_JOIN = "NO_JOIN";
  private static final String TRINO_URL = "jdbc:trino://%s/%s/%s?SSL=true&SSLVerification=NONE";

  public static void main(String[] args) throws Exception {
    Properties properties = PropertiesUtil.readFromFile("/etc/gbif/config.properties");

    GrSciCollLookupIndexingOptions options =
        PipelineOptionsFactory.as(GrSciCollLookupIndexingOptions.class);

    options.setTrinoServer(properties.getProperty("trinoServer"));
    options.setTrinoUser(properties.getProperty("trinoUser"));
    options.setTrinoPassword(properties.getProperty("trinoPassword"));
    options.setTrinoDbName(properties.getProperty("trinoDbName"));
    options.setTrinoTargetTable(properties.getProperty("trinoTargetTable"));

    options.setHbaseZk(properties.getProperty("hbaseZk"));
    options.setSaltedKeyBuckets(Integer.parseInt(properties.getProperty("hbaseSaltedKeyBuckets")));
    options.setTargetTable(properties.getProperty("hbaseTargetTable"));
    options.setHbaseZkNode(properties.getProperty("hbaseZkNode"));

    options.setBaseApiUrl(properties.getProperty("apiBaseUrl"));
    options.setApiTimeOut(Long.parseLong(properties.getProperty("apiTimeOut")));
    options.setRestClientCacheMaxSize(
        Long.parseLong(properties.getProperty("apiRestClientCacheMaxSize")));

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
   * Removes from the HBase table the entries that are not present in the Hive table that contains
   * all the unique requests present in the occurrence data.
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

    final SaltedKeyGenerator keyGenerator =
        new SaltedKeyGenerator(
            storeConfiguration.getHBaseKVStoreConfiguration().getNumOfKeyBuckets());

    // read records from Trino
    String trinoUrl =
        String.format(
            TRINO_URL,
            options.getTrinoServer(),
            options.getTrinoCatalog(),
            options.getTrinoDbName());

    PCollection<KV<String, GrscicollLookupRequest>> trinoRecords =
        pipeline.apply(
            JdbcIO.<KV<String, GrscicollLookupRequest>>read()
                .withDataSourceConfiguration(
                    JdbcIO.DataSourceConfiguration.create("io.trino.jdbc.TrinoDriver", trinoUrl)
                        .withUsername(options.getTrinoUser())
                        .withPassword(options.getTrinoPassword()))
                .withQuery("select * from " + options.getTrinoTargetTable())
                .withRowMapper(
                    resultSet -> {
                      GrscicollLookupRequest request =
                          GrscicollLookupRequest.builder()
                              .withOwnerInstitutionCode(resultSet.getString("ownerInstitutionCode"))
                              .withInstitutionCode(resultSet.getString("institutionCode"))
                              .withInstitutionId(resultSet.getString("institutionId"))
                              .withCollectionCode(resultSet.getString("collectionCode"))
                              .withCollectionId(resultSet.getString("collectionId"))
                              .withDatasetKey(resultSet.getString("datasetKey"))
                              .withCountry(resultSet.getString("country"))
                              .build();

                      String key =
                          new String(
                              keyGenerator.computeKey(request.getLogicalKey()),
                              keyGenerator.getCharset());

                      return KV.of(key, request);
                    })
                .withCoder(
                    KvCoder.of(
                        StringUtf8Coder.of(), SerializableCoder.of(GrscicollLookupRequest.class))));

    // convert hive keys to KVs
    PCollection<KV<String, String>> hiveKVs =
        trinoRecords
            .apply(Keys.create())
            .apply(WithKeys.of(input -> input))
            .setCoder(KvCoder.of(StringUtf8Coder.of(), StringUtf8Coder.of()));

    // read records from HBase
    Scan scan = new Scan();
    scan.setBatch(10000); // for safety
    scan.addFamily(options.getKVColumnFamily().getBytes());

    PCollection<String> hbaseKeys =
        pipeline
            .apply(
                "Read HBase",
                HBaseIO.read()
                    .withConfiguration(hBaseConfiguration)
                    .withScan(scan)
                    .withTableId(storeConfiguration.getHBaseKVStoreConfiguration().getTableName()))
            .apply(
                ParDo.of(
                    new DoFn<Result, String>() {
                      @ProcessElement
                      public void processElement(ProcessContext context) {
                        Result hbaseRecord = context.element();
                        String key = new String(hbaseRecord.getRow(), keyGenerator.getCharset());
                        context.output(key);
                      }
                    }));

    // convert hbase keys them to KVs
    PCollection<KV<String, String>> hbaseKVs =
        hbaseKeys
            .apply(WithKeys.of(input -> input))
            .setCoder(KvCoder.of(hbaseKeys.getCoder(), hbaseKeys.getCoder()));

    // do a left join being the hbase keys on the left side
    PCollection<KV<String, KV<String, String>>> join =
        Join.leftOuterJoin(hbaseKVs, hiveKVs, NULL_VALUE_IN_JOIN);

    // delete from hbase the records that don't have a match in the hive keys (right side of the
    // join)
    join.apply(
            ParDo.of(
                new DoFn<KV<String, KV<String, String>>, Mutation>() {
                  @ProcessElement
                  public void processElement(ProcessContext context) {
                    KV<String, KV<String, String>> joinRecord = context.element();
                    if (joinRecord.getValue().getValue().equals(NULL_VALUE_IN_JOIN)) {
                      Delete delete =
                          new Delete(joinRecord.getKey().getBytes(keyGenerator.getCharset()));
                      context.output(delete);
                    }
                  }
                }))
        .apply( // Write to HBase
            HBaseIO.write()
                .withConfiguration(hBaseConfiguration)
                .withTableId(storeConfiguration.getHBaseKVStoreConfiguration().getTableName()));

    // Perform GRSciColl lookup
    trinoRecords
        .apply(
            ParDo.of(
                new DoFn<KV<String, GrscicollLookupRequest>, Mutation>() {

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
                      GrscicollLookupRequest req = context.element().getValue();

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
                      log.error("Error performing GrSciColl lookup", ex);
                    }
                  }

                  @Teardown
                  public void tearDown() {
                    if (Objects.nonNull(lookupService)) {
                      try {
                        lookupService.close();
                      } catch (IOException ex) {
                        log.error("Error closing lookup service", ex);
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
