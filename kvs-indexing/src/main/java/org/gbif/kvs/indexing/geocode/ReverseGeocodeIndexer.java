package org.gbif.kvs.indexing.geocode;

import org.gbif.kvs.SaltedKeyGenerator;
import org.gbif.kvs.geocode.GeocodeKVStoreConfiguration;
import org.gbif.kvs.geocode.GeocodeKVStoreFactory;
import org.gbif.kvs.geocode.LatLng;
import org.gbif.kvs.indexing.options.ConfigurationMapper;
import org.gbif.rest.client.configuration.ClientConfiguration;
import org.gbif.rest.client.geocode.GeocodeResponse;
import org.gbif.rest.client.geocode.Location;
import org.gbif.rest.client.geocode.GeocodeService;
import org.gbif.rest.client.geocode.retrofit.GeocodeServiceSyncClient;

import java.util.Collection;
import java.util.Objects;
import java.util.Optional;
import java.util.function.BiFunction;

import org.apache.beam.runners.spark.SparkRunner;
import org.apache.beam.sdk.Pipeline;
import org.apache.beam.sdk.PipelineResult;
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
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.util.Bytes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Apache Beam Pipeline that indexes Geocode country lookup responses in a HBase KV table. */
public class ReverseGeocodeIndexer {

  private static final Logger LOG = LoggerFactory.getLogger(ReverseGeocodeIndexer.class);

  public static void main(String[] args) {
    GeocodeIndexingOptions options =
        PipelineOptionsFactory.fromArgs(args).withValidation().as(GeocodeIndexingOptions.class);
    run(options);
  }

  /**
   * Creates a {@link GeocodeKVStoreConfiguration} from a {@link GeocodeIndexingOptions} instance.
   *
   * @param options pipeline options
   * @return a new instance of GeocodeKVStoreConfiguration
   */
  private static GeocodeKVStoreConfiguration geocodeKVStoreConfiguration(GeocodeIndexingOptions options) {
    return GeocodeKVStoreConfiguration.builder()
            .withHBaseKVStoreConfiguration(ConfigurationMapper.hbaseKVStoreConfiguration(options))
            .withJsonColumnQualifier(options.getJsonColumnQualifier())
            .build();
  }

  /**
   * Runs the indexing beam pipeline. 1. Reads all latitude and longitude from the occurrence table.
   * 2. Selects only distinct coordinates 3. Store the Geocode country lookup in table with the KV
   * format: latitude+longitude -> isoCountryCode2Digit.
   *
   * @param options beam HBase indexing options
   */
  private static void run(GeocodeIndexingOptions options) {

    Pipeline pipeline = Pipeline.create(options);
    options.setRunner(SparkRunner.class);

    // Occurrence table to read
    String sourceTable = options.getSourceTable();

    // Config
    GeocodeKVStoreConfiguration storeConfiguration = geocodeKVStoreConfiguration(options);
    ClientConfiguration geocodeClientConfiguration = ConfigurationMapper.clientConfiguration(options);
    Configuration hBaseConfiguration = storeConfiguration.getHBaseKVStoreConfiguration().hbaseConfig();

    // Reade the occurrence table
    PCollection<Result> inputRecords =
        pipeline.apply(
            HBaseIO.read().withConfiguration(hBaseConfiguration).withTableId(sourceTable));
    // Select distinct coordinates
    PCollection<LatLng> distinctCoordinates =
        inputRecords
            .apply(
                ParDo.of(
                    new DoFn<Result, LatLng>() {

                      @ProcessElement
                      public void processElement(ProcessContext context) {
                        LatLng latLng = OccurrenceHBaseBuilder.toLatLng(context.element());
                        if (latLng.isValid()) {
                          context.output(latLng);
                        }
                      }
                      // Selects distinct values
                    }))
            .apply(
                Distinct.<LatLng, String>withRepresentativeValueFn(LatLng::getLogicalKey)
                    .withRepresentativeType(TypeDescriptor.of(String.class)));

    // Perform Geocode lookup
    distinctCoordinates
        .apply(
            ParDo.of(
                new DoFn<LatLng, Mutation>() {

                  private final SaltedKeyGenerator keyGenerator =
                      new SaltedKeyGenerator(
                          storeConfiguration.getHBaseKVStoreConfiguration().getNumOfKeyBuckets());

                  private transient GeocodeService geocodeService;

                  private transient BiFunction<byte[], GeocodeResponse, Put> valueMutator;

                  @Setup
                  public void start() {
                    geocodeService = new GeocodeServiceSyncClient(geocodeClientConfiguration);
                    valueMutator =
                        GeocodeKVStoreFactory.valueMutator(
                            Bytes.toBytes(storeConfiguration.getHBaseKVStoreConfiguration().getColumnFamily()),
                            Bytes.toBytes(storeConfiguration.getJsonColumnQualifier()));
                  }

                  @ProcessElement
                  public void processElement(ProcessContext context) {
                    try {
                      LatLng latLng = context.element();
                      Optional.ofNullable(geocodeService.reverse(latLng.getLatitude(), latLng.getLongitude()))
                              .ifPresent( locations -> {
                                  GeocodeResponse response = new GeocodeResponse(geocodeService.reverse(latLng.getLatitude(), latLng.getLongitude()));
                                  byte[] saltedKey = keyGenerator.computeKey(latLng.getLogicalKey());
                                  context.output(valueMutator.apply(saltedKey, response));
                              });
                    } catch (Exception ex) {
                      LOG.error("Error performing Geocode lookup", ex);
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
