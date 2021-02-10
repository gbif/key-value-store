package org.gbif.kvs.indexing.grscicoll;

import java.util.Collections;

import org.gbif.kvs.SaltedKeyGenerator;
import org.gbif.kvs.conf.CachedHBaseKVStoreConfiguration;
import org.gbif.kvs.grscicoll.GrscicollLookupRequest;
import org.gbif.kvs.indexing.options.ConfigurationMapper;

import org.apache.beam.runners.spark.SparkRunner;
import org.apache.beam.sdk.Pipeline;
import org.apache.beam.sdk.PipelineResult;
import org.apache.beam.sdk.coders.KvCoder;
import org.apache.beam.sdk.extensions.joinlibrary.Join;
import org.apache.beam.sdk.io.hbase.HBaseIO;
import org.apache.beam.sdk.io.hcatalog.HCatalogIO;
import org.apache.beam.sdk.options.PipelineOptionsFactory;
import org.apache.beam.sdk.transforms.DoFn;
import org.apache.beam.sdk.transforms.ParDo;
import org.apache.beam.sdk.transforms.WithKeys;
import org.apache.beam.sdk.values.KV;
import org.apache.beam.sdk.values.PCollection;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.client.Delete;
import org.apache.hadoop.hbase.client.Mutation;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.Scan;
import org.apache.hadoop.hive.conf.HiveConf.ConfVars;
import org.apache.hive.hcatalog.data.HCatRecord;
import org.apache.hive.hcatalog.data.schema.HCatSchema;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.gbif.kvs.indexing.grscicoll.HiveUtils.readSchema;

/** Apache Beam Pipeline that removes unused values from the HBase KV table. */
public class GrscicollLookupCleaner {

  private static final Logger LOG = LoggerFactory.getLogger(GrscicollLookupCleaner.class);

  private static final String NULL_VALUE_IN_JOIN = "NO_JOIN";

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
    Configuration hBaseConfiguration =
        storeConfiguration.getHBaseKVStoreConfiguration().hbaseConfig();

    final SaltedKeyGenerator keyGenerator =
        new SaltedKeyGenerator(
            storeConfiguration.getHBaseKVStoreConfiguration().getNumOfKeyBuckets());

    final HCatSchema schema = readSchema(options);

    // read records from Hive
    PCollection<String> hiveKeys =
        pipeline
            .apply(
                HCatalogIO.read()
                    .withConfigProperties(
                        Collections.singletonMap(
                            ConfVars.METASTOREURIS.varname, options.getMetastoreUris()))
                    .withDatabase(options.getDatabase())
                    .withTable(options.getTable()))
            .apply(
                ParDo.of(
                    new DoFn<HCatRecord, String>() {

                      @ProcessElement
                      public void processElement(ProcessContext context) {
                        try {
                          HCatRecord record = context.element();
                          GrscicollLookupRequest req =
                              HiveUtils.convertToGrSciCollRequest(record, schema);
                          String key =
                              new String(
                                  keyGenerator.computeKey(req.getLogicalKey()),
                                  keyGenerator.getCharset());
                          context.output(key);
                        } catch (Exception ex) {
                          LOG.error("Error creating grscicoll request", ex);
                        }
                      }
                    }));

    // convert hive keys them to KVs
    PCollection<KV<String, String>> hiveKVs =
        hiveKeys
            .apply(WithKeys.of(input -> input))
            .setCoder(KvCoder.of(hiveKeys.getCoder(), hiveKeys.getCoder()));

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

    // Run and wait
    PipelineResult result = pipeline.run(options);
    result.waitUntilFinish();
  }
}
