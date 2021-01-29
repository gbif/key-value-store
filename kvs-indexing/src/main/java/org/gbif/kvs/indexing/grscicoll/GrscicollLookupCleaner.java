package org.gbif.kvs.indexing.grscicoll;

import java.util.Collections;
import java.util.Map;

import org.gbif.kvs.SaltedKeyGenerator;
import org.gbif.kvs.conf.CachedHBaseKVStoreConfiguration;
import org.gbif.kvs.grscicoll.GrscicollLookupRequest;
import org.gbif.kvs.indexing.options.ConfigurationMapper;

import org.apache.beam.runners.spark.SparkRunner;
import org.apache.beam.sdk.Pipeline;
import org.apache.beam.sdk.PipelineResult;
import org.apache.beam.sdk.coders.KvCoder;
import org.apache.beam.sdk.coders.StringUtf8Coder;
import org.apache.beam.sdk.io.hbase.HBaseIO;
import org.apache.beam.sdk.io.hcatalog.HCatalogIO;
import org.apache.beam.sdk.options.PipelineOptionsFactory;
import org.apache.beam.sdk.transforms.DoFn;
import org.apache.beam.sdk.transforms.ParDo;
import org.apache.beam.sdk.transforms.View;
import org.apache.beam.sdk.transforms.WithKeys;
import org.apache.beam.sdk.values.PCollection;
import org.apache.beam.sdk.values.PCollectionView;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.client.Delete;
import org.apache.hadoop.hbase.client.Mutation;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.util.Bytes;
import org.apache.hadoop.hive.conf.HiveConf.ConfVars;
import org.apache.hive.hcatalog.data.HCatRecord;
import org.apache.hive.hcatalog.data.schema.HCatSchema;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.gbif.kvs.indexing.grscicoll.HiveUtils.readSchema;

/** Apache Beam Pipeline that removes unused values from the HBase KV table. */
public class GrscicollLookupCleaner {

  private static final Logger LOG = LoggerFactory.getLogger(GrscicollLookupCleaner.class);

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
    Configuration hBaseConfiguration =
        storeConfiguration.getHBaseKVStoreConfiguration().hbaseConfig();

    final SaltedKeyGenerator keyGenerator =
        new SaltedKeyGenerator(
            storeConfiguration.getHBaseKVStoreConfiguration().getNumOfKeyBuckets());

    final HCatSchema schema = readSchema(options);

    PCollection<GrscicollLookupRequest> hiveRequests =
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
                    new DoFn<HCatRecord, GrscicollLookupRequest>() {

                      @ProcessElement
                      public void processElement(ProcessContext context) {
                        try {
                          HCatRecord record = context.element();
                          GrscicollLookupRequest req =
                              HiveUtils.convertToGrSciCollRequest(record, schema);
                          context.output(req);
                        } catch (Exception ex) {
                          LOG.error("Error creating grscicoll request", ex);
                        }
                      }
                    }));

    // read records from Hive and create a map view indexed by the salted keys used in HBase
    PCollectionView<Map<String, GrscicollLookupRequest>> hiveRecordsView =
        hiveRequests
            .apply(WithKeys.of(input -> new String(keyGenerator.computeKey(input.getLogicalKey()))))
            .setCoder(KvCoder.of(StringUtf8Coder.of(), hiveRequests.getCoder()))
            .apply(View.asMap());

    // read records from HBase
    PCollection<Result> rows =
        pipeline.apply(
            "Read HBase",
            HBaseIO.read()
                .withConfiguration(hBaseConfiguration)
                .withTableId(storeConfiguration.getHBaseKVStoreConfiguration().getTableName()));

    // delete from HBase the records that are not in the map view
    rows.apply(
            ParDo.of(
                    new DoFn<Result, Mutation>() {
                      @ProcessElement
                      public void processElement(ProcessContext context) {
                        Map<String, GrscicollLookupRequest> hiveRecords =
                            context.sideInput(hiveRecordsView);

                        Result hbaseRecord = context.element();
                        if (!hiveRecords.containsKey(new String(hbaseRecord.getRow()))) {
                          Delete delete = new Delete(hbaseRecord.getRow());
                          delete.addColumn(
                              Bytes.toBytes(
                                  storeConfiguration
                                      .getHBaseKVStoreConfiguration()
                                      .getColumnFamily()),
                              Bytes.toBytes(storeConfiguration.getValueColumnQualifier()));
                          context.output(delete);
                        }
                      }
                    })
                .withSideInputs(hiveRecordsView))
        .apply( // Write to HBase
            HBaseIO.write()
                .withConfiguration(hBaseConfiguration)
                .withTableId(storeConfiguration.getHBaseKVStoreConfiguration().getTableName()));

    // Run and wait
    PipelineResult result = pipeline.run(options);
    result.waitUntilFinish();
  }
}
