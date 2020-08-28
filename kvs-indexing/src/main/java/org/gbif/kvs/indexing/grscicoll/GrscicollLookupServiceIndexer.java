package org.gbif.kvs.indexing.grscicoll;

import java.util.Objects;
import java.util.UUID;
import java.util.function.BiFunction;

import org.gbif.kvs.SaltedKeyGenerator;
import org.gbif.kvs.conf.CachedHBaseKVStoreConfiguration;
import org.gbif.kvs.grscicoll.GrscicollLookupRequest;
import org.gbif.kvs.grscicoll.GrscicollLookupKVStoreFactory;
import org.gbif.kvs.indexing.options.ConfigurationMapper;
import org.gbif.rest.client.configuration.ClientConfiguration;
import org.gbif.rest.client.grscicoll.GrscicollLookupResponse;
import org.gbif.rest.client.grscicoll.GrscicollLookupService;
import org.gbif.rest.client.grscicoll.retrofit.GrscicollLookupServiceSyncClient;

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

/** Apache Beam Pipeline that indexes GrSciColl lookup responses in a HBase KV table. */
public class GrscicollLookupServiceIndexer {

  private static final Logger LOG = LoggerFactory.getLogger(GrscicollLookupServiceIndexer.class);

  public static void main(String[] args) {
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
   * response in a HBase table with the KV format: {@link GrscicollLookupRequest#getLogicalKey()} -> JSON
   * response.
   *
   * @param options beam HBase indexing options
   */
  private static void run(GrSciCollLookupIndexingOptions options) {

    Pipeline pipeline = Pipeline.create(options);
    options.setRunner(SparkRunner.class);

    // Occurrence table to read
    String sourceGlob = options.getSourceGlob();

    // Config
    CachedHBaseKVStoreConfiguration storeConfiguration = grSciCollKVStoreConfiguration(options);
    ClientConfiguration grSciCollClientConfiguration =
        ConfigurationMapper.clientConfiguration(options);
    Configuration hBaseConfiguration =
        storeConfiguration.getHBaseKVStoreConfiguration().hbaseConfig();

    // Read the occurrence table
    PCollection<GrscicollLookupRequest> inputRecords =
        pipeline.apply(
            AvroIO.parseGenericRecords(new AvroOccurrenceRecordToLookupRequest())
                .withCoder(AvroCoder.of(GrscicollLookupRequest.class))
                .from(sourceGlob));

    // Select distinct request
    PCollection<GrscicollLookupRequest> distinctRequests =
        inputRecords.apply(
            Distinct.<GrscicollLookupRequest, String>withRepresentativeValueFn(GrscicollLookupRequest::getLogicalKey)
                .withRepresentativeType(TypeDescriptor.of(String.class)));

    // Perform Geocode lookup
    distinctRequests
        .apply(
            ParDo.of(
                new DoFn<GrscicollLookupRequest, Mutation>() {

                  private final SaltedKeyGenerator keyGenerator =
                      new SaltedKeyGenerator(
                          storeConfiguration.getHBaseKVStoreConfiguration().getNumOfKeyBuckets());

                  private transient GrscicollLookupService lookupService;

                  private transient BiFunction<byte[], GrscicollLookupResponse, Put> valueMutator;

                  @Setup
                  public void start() {
                    lookupService = new GrscicollLookupServiceSyncClient(grSciCollClientConfiguration);
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
                      GrscicollLookupRequest req = context.element();
                      GrscicollLookupResponse lookupResponse =
                          lookupService.lookup(
                              req.getInstitutionCode(),
                              req.getOwnerInstitutionCode(),
                              req.getInstitutionId(),
                              req.getCollectionCode(),
                              req.getCollectionId(),
                              req.getDatasetKey() != null ? UUID.fromString(req.getDatasetKey()) : null,
                              // country is always null so we avoid parsing it
                              null);
                      if (Objects.nonNull(lookupResponse)) {
                        byte[] saltedKey = keyGenerator.computeKey(req.getLogicalKey());
                        context.output(valueMutator.apply(saltedKey, lookupResponse));
                      }
                    } catch (Exception ex) {
                      LOG.error("Error performing Geocode lookup", ex);
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
