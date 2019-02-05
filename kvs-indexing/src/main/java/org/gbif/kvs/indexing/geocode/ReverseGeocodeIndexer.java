package org.gbif.kvs.indexing.geocode;

import org.gbif.kvs.SaltedKeyGenerator;
import org.gbif.kvs.geocode.GeocodeKVStoreConfiguration;
import org.gbif.kvs.geocode.GeocodeKVStoreFactory;
import org.gbif.kvs.geocode.LatLng;
import org.gbif.kvs.indexing.options.ConfigurationMapper;
import org.gbif.rest.client.geocode.GeocodeResponse;
import org.gbif.rest.client.geocode.GeocodeService;
import org.gbif.rest.client.geocode.GeocodeServiceFactory;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import retrofit2.Response;

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
  private static GeocodeKVStoreConfiguration geocodeKVStoreConfiguration(
      GeocodeIndexingOptions options) {
    return GeocodeKVStoreConfiguration.builder()
            .withHBaseKVStoreConfiguration(ConfigurationMapper.hbaseKVStoreConfiguration(options))
            .withGeocodeClientConfig(ConfigurationMapper.clientConfiguration(options))
            .withCountryCodeColumnQualifier(options.getCountryCodeColumnQualifier())
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
    Configuration hBaseConfiguration =
        storeConfiguration.getHBaseKVStoreConfiguration().hbaseConfig();

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
                Distinct.<LatLng, String>withRepresentativeValueFn(
                        latLng -> new String(latLng.getLogicalKey(), StandardCharsets.UTF_8))
                    .withRepresentativeType(TypeDescriptor.of(String.class)));

    // Perform Geocode lookup
    distinctCoordinates
        .apply(
            ParDo.of(
                new DoFn<LatLng, Mutation>() {

                  private final SaltedKeyGenerator keyGenerator =
                      new SaltedKeyGenerator(
                          storeConfiguration.getHBaseKVStoreConfiguration().getNumOfKeyBuckets());

                  private GeocodeService geocodeService;

                  private BiFunction<byte[], Collection<GeocodeResponse>, Put> valueMutator;

                  @Setup
                  public void start() {
                    geocodeService =
                        GeocodeServiceFactory.createGeocodeServiceClient(
                            storeConfiguration.getGeocodeClientConfiguration());
                    valueMutator =
                        GeocodeKVStoreFactory.valueMutator(
                            storeConfiguration
                                .getHBaseKVStoreConfiguration()
                                .getColumnFamily()
                                .getBytes(StandardCharsets.UTF_8),
                            storeConfiguration.getCountryCodeColumnQualifier().getBytes(StandardCharsets.UTF_8),
                            storeConfiguration.getJsonColumnQualifier().getBytes(StandardCharsets.UTF_8));
                  }

                  /**
                   * Performs the Geocode lookup.
                   *
                   * @param latLng to lookup
                   * @return an Optional.of(countryCode), empty() otherwise
                   * @throws IOException in case of communication error
                   */
                  private Optional<Collection<GeocodeResponse>> callGeocodeService(LatLng latLng)
                      throws IOException {
                    Response<Collection<GeocodeResponse>> response =
                        geocodeService
                            .reverse(latLng.getLatitude(), latLng.getLongitude())
                            .execute();
                    if (response.isSuccessful()
                        && Objects.nonNull(response.body())
                        && !response.body().isEmpty()) {
                      return Optional.of(response.body());
                    }
                    return Optional.empty();
                  }

                  @ProcessElement
                  public void processElement(ProcessContext context) {
                    try {
                      LatLng latLng = context.element();
                      callGeocodeService(latLng)
                          .ifPresent(
                              geocodeResponses -> {
                                byte[] saltedKey = keyGenerator.computeKey(latLng.getLogicalKey());
                                context.output(valueMutator.apply(saltedKey, geocodeResponses));
                              });
                    } catch (IOException ex) {
                      LOG.error("Error performing Geocode lookup", ex);
                    }
                  }
                  // Write to HBase
                }))
        .apply(
            HBaseIO.write()
                .withConfiguration(hBaseConfiguration)
                .withTableId(storeConfiguration.getHBaseKVStoreConfiguration().getTableName()));

    // Run and wait
    PipelineResult result = pipeline.run(options);
    result.waitUntilFinish();
  }
}
