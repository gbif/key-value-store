package org.gbif.kvs.indexing.geocode;

import org.gbif.kvs.SaltedKeyGenerator;
import org.gbif.kvs.geocode.LatLng;
import org.gbif.kvs.hbase.HBaseKVStoreConfiguration;
import org.gbif.rest.client.config.ClientConfig;
import org.gbif.rest.client.geocode.GeocodeResponse;
import org.gbif.rest.client.geocode.GeocodeService;
import org.gbif.rest.client.geocode.GeocodeServiceFactory;

import java.io.IOException;
import java.util.Collection;
import java.util.Objects;
import java.util.Optional;

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

/**
 * Apache Beam Pipeline that indexes Geocode country lookup responses in a HBase KV table.
 */
public class ReverseGeocodeIndexer {

    private static final Logger LOG = LoggerFactory.getLogger(ReverseGeocodeIndexer.class);


    public static void main(String[] args) {
        HBaseIndexerOptions options = PipelineOptionsFactory
                                            .fromArgs(args)
                                            .withValidation().as(HBaseIndexerOptions.class);
        run(options);
    }

    /**
     * Creates a {@link HBaseKVStoreConfiguration} from a {@link HBaseIndexerOptions} instance.
     * @param options pipeline options
     * @return a new instance of HBaseKVStoreConfiguration
     */
    private static HBaseKVStoreConfiguration hBaseKVStoreConfiguration(HBaseIndexerOptions options) {
        return new HBaseKVStoreConfiguration.Builder()
                .withTableName(options.getTargetTable())
                .withColumnFamily(options.getKVColumnFamily())
                .withQualifier(options.getValueColumnQualifier())
                .withHBaseZk(options.getHBaseZk())
                .withNumOfKeyBuckets(options.getSaltedKeyBuckets())
                .build();
    }

    /**
     * Creates a {@link ClientConfig} from a {@link HBaseIndexerOptions} instance.
     * @param options pipeline options
     * @return a new instance of ClientConfig
     */
    private static ClientConfig clientConfig(HBaseIndexerOptions options) {
        return new ClientConfig.Builder()
                    .setBaseApiUrl(options.getBaseApiUrl())
                    .setTimeOut(options.getApiTimeOut())
                    .setFileCacheMaxSizeMb(options.getRestClientCacheMaxSize())
                    .build();
    }

    /**
     * Runs the indexing beam pipeline.
     * 1. Reads all latitude and longitude from the occurrence table.
     * 2. Selects only distinct coordinates
     * 3. Store the Geocode country lookup in table with the KV format: latitude+longitude -> isoCountryCode2Digit.
     *
     * @param options beam HBase indexing options
     */
    private static void run(HBaseIndexerOptions options) {

        Pipeline pipeline = Pipeline.create(options);
        options.setRunner(SparkRunner.class);

        //Occurrence table to read
        String sourceTable = options.getSourceTable();

        //Config
        ClientConfig clientConfig = clientConfig(options);
        HBaseKVStoreConfiguration kvStoreConfiguration = hBaseKVStoreConfiguration(options);
        Configuration hBaseConfiguration = kvStoreConfiguration.hbaseConfig();

        //Reade the occurrence table
        PCollection<Result> inputRecords = pipeline.apply(HBaseIO.read()
                                                            .withConfiguration(hBaseConfiguration)
                                                            .withTableId(sourceTable));
        //Select distinct coordinates
        PCollection<LatLng> distinctCoordinates = inputRecords.apply(ParDo.of( new DoFn<Result,LatLng>() {

            @ProcessElement
            public void processElement(ProcessContext context) {
                LatLng latLng = OccurrenceHBaseBuilder.toLatLng(context.element());
                if (latLng.isValid()) {
                    context.output(latLng);
                }
            }
            //Selects distinct values
        })).apply(Distinct.<LatLng,String>withRepresentativeValueFn(latLng -> new String(latLng.getLogicalKey()))
                          .withRepresentativeType(TypeDescriptor.of(String.class)));

        //Perform Geocode lookup
        distinctCoordinates.apply(ParDo.of(new DoFn<LatLng, Mutation>() {

            private final SaltedKeyGenerator keyGenerator = new SaltedKeyGenerator(kvStoreConfiguration.getNumOfKeyBuckets());

            private GeocodeService geocodeService;

            @Setup
            public void start() {
                geocodeService = GeocodeServiceFactory.createGeocodeServiceClient(clientConfig);
            }

            /**
             * Performs the Geocode lookup.
             * @param latLng to lookup
             * @return an Optional.of(countryCode), empty() otherwise
             * @throws IOException in case of communication error
             */
            private Optional<String> getFirstCountryCode(LatLng latLng) throws IOException {
                Response<Collection<GeocodeResponse>> response = geocodeService.reverse(latLng.getLatitude(),
                                                                                        latLng.getLatitude()).execute();
                if (response.isSuccessful() && Objects.nonNull(response.body()) && !response.body().isEmpty()) {
                    return Optional.ofNullable(response.body().iterator().next().getIsoCountryCode2Digit());
                }
                return Optional.empty();
            }

            @ProcessElement
            public void processElement(ProcessContext context) {
                try {
                    LatLng latLng = context.element();
                    getFirstCountryCode(latLng)
                            .ifPresent(countryCode -> {
                                byte[] saltedKey = keyGenerator.computeKey(latLng.getLogicalKey());
                                Put put = new Put(saltedKey);
                                put.addColumn(kvStoreConfiguration.getColumnFamily().getBytes(),
                                              kvStoreConfiguration.getQualifier().getBytes(),
                                              countryCode.getBytes());
                                context.output(put);
                            });
                } catch (IOException ex) {
                    LOG.error("Error performing Geocode lookup", ex);
                }

            }
            //Write to HBase
        })).apply(HBaseIO.write()
                    .withConfiguration(hBaseConfiguration)
                    .withTableId(kvStoreConfiguration.getTableName()));

        //Run and wait
        PipelineResult result = pipeline.run(options);
        result.waitUntilFinish();

    }

}
