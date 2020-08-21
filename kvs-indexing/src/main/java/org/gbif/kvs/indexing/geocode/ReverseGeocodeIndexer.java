package org.gbif.kvs.indexing.geocode;

import org.gbif.dwc.terms.DwcTerm;
import org.gbif.dwc.terms.Term;
import org.gbif.kvs.SaltedKeyGenerator;
import org.gbif.kvs.conf.CachedHBaseKVStoreConfiguration;
import org.gbif.kvs.geocode.GeocodeKVStoreFactory;
import org.gbif.kvs.geocode.LatLng;
import org.gbif.kvs.indexing.options.ConfigurationMapper;
import org.gbif.rest.client.configuration.ClientConfiguration;
import org.gbif.rest.client.geocode.GeocodeResponse;
import org.gbif.rest.client.geocode.GeocodeService;
import org.gbif.rest.client.geocode.retrofit.GeocodeServiceSyncClient;

import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Consumer;

import org.apache.avro.generic.GenericRecord;
import org.apache.beam.sdk.coders.AvroCoder;
import org.apache.beam.sdk.io.AvroIO;
import org.apache.beam.sdk.transforms.SerializableFunction;
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
import org.apache.hadoop.hbase.util.Bytes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Apache Beam Pipeline that indexes Geocode country lookup responses in a HBase KV table. */
public class ReverseGeocodeIndexer {

  private static final Logger LOG = LoggerFactory.getLogger(ReverseGeocodeIndexer.class);
  private static final String DECIMAL_LATITUDE = DwcTerm.decimalLatitude.simpleName().toLowerCase();
  private static final String DECIMAL_LONGITUDE = DwcTerm.decimalLongitude.simpleName().toLowerCase();

  public static void main(String[] args) {
    GeocodeIndexingOptions options =
        PipelineOptionsFactory.fromArgs(args).withValidation().as(GeocodeIndexingOptions.class);
    run(options);
  }

  /**
   * Creates a {@link CachedHBaseKVStoreConfiguration} from a {@link GeocodeIndexingOptions} instance.
   *
   * @param options pipeline options
   * @return a new instance of GeocodeKVStoreConfiguration
   */
  private static CachedHBaseKVStoreConfiguration geocodeKVStoreConfiguration(GeocodeIndexingOptions options) {
    return CachedHBaseKVStoreConfiguration.builder()
            .withHBaseKVStoreConfiguration(ConfigurationMapper.hbaseKVStoreConfiguration(options))
            .withValueColumnQualifier(options.getJsonColumnQualifier())
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
    String sourceGlob = options.getSourceGlob();

    // Config
    CachedHBaseKVStoreConfiguration storeConfiguration = geocodeKVStoreConfiguration(options);
    ClientConfiguration geocodeClientConfiguration = ConfigurationMapper.clientConfiguration(options);
    Configuration hBaseConfiguration = storeConfiguration.getHBaseKVStoreConfiguration().hbaseConfig();

    // Retrieve just the latitude and longitude from the Avro record
    SerializableFunction<GenericRecord, LatLng> recordToLatLng = input -> {
      LatLng.Builder builder = LatLng.builder();
      putIfExists(input, DwcTerm.decimalLatitude, builder::withLatitude);
      putIfExists(input, DwcTerm.decimalLongitude, builder::withLongitude);
      return builder.build();
    };

    // Read the occurrence table
    PCollection<LatLng> inputRecords =
      pipeline.apply(AvroIO.parseGenericRecords(recordToLatLng)
          .withCoder(AvroCoder.of(LatLng.class))
          .from(sourceGlob)
      );

    // Select distinct coordinates
    PCollection<LatLng> distinctCoordinates =
        inputRecords
            .apply(
                ParDo.of(
                    new DoFn<LatLng, LatLng>() {
                      @ProcessElement
                      public void processElement(ProcessContext context) {
                        LatLng latLng = context.element();
                        if (latLng.isValid()) {
                          context.output(latLng);
                        }
                      }
                    }))
            .apply(
                // Selects distinct values
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
                            Bytes.toBytes(storeConfiguration.getValueColumnQualifier()));
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

  /**
   * Reads the double value associated to a term into the consumer 'with'.
   * @param input Avro record
   * @param term verbatim field/term
   * @param with consumer
   */
  private static void putIfExists(GenericRecord input, Term term, Consumer<Double> with) {
    Optional.ofNullable(input.get(term.simpleName().toLowerCase()))
      .map(Object::toString)
      .map(Double::parseDouble)
      .ifPresent(with);
  }
}
