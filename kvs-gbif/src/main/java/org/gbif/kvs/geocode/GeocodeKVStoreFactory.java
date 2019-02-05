package org.gbif.kvs.geocode;

import org.gbif.kvs.KeyValueStore;
import org.gbif.kvs.hbase.HBaseKVStoreConfiguration;
import org.gbif.kvs.hbase.HBaseStore;
import org.gbif.rest.client.config.ClientConfig;
import org.gbif.rest.client.geocode.GeocodeResponse;
import org.gbif.rest.client.geocode.GeocodeService;
import org.gbif.rest.client.geocode.GeocodeServiceFactory;

import java.io.IOException;
import java.util.Collection;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Function;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.util.Bytes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import retrofit2.Response;

/**
 * Factory of Geocode KV instances.
 */
public class GeocodeKVStoreFactory {

    private static final Logger LOG = LoggerFactory.getLogger(GeocodeServiceFactory.class);

    //Used to store and retrieve JSON values stored in HBase
    private static final ObjectMapper MAPPER = new ObjectMapper();

    {
        MAPPER.setSerializationInclusion(JsonInclude.Include.NON_NULL);
    }

    /**
     * Hidden constructor.
     */
    private GeocodeKVStoreFactory() {
        //DO NOTHING
    }


    /**
     * Returns a function that maps HBase results into single String values.
     * @param columnFamily HBase column in which values are stored
     * @param columnQualifier HBase column qualifier in which values are stored
     * @return
     */
    public static Function<Result, String> simpleResultMapper(byte[] columnFamily, byte[] columnQualifier) {
        return result ->  Bytes.toString(result.getValue(columnFamily, columnQualifier));
    }

    /**
     * Function that extracts the first country code of a list of {@link GeocodeResponse}.
     * @return a function that extracts the first country code
     */
    public static Function<Collection<GeocodeResponse>, String> countryCodeMapper() {
        return geocodeResponses -> {
            GeocodeResponse firstCountry = geocodeResponses.iterator().next();
            return firstCountry.getIsoCountryCode2Digit();
        };
    }

    /**
     * Creates a mutator function that maps a key and a list of {@link GeocodeResponse} into a {@link Put}.
     * @param columnFamily HBase column in which values are stored
     * @param countryCodeColumnQualifier HBase column qualifier in which the country code is stored
     * @param jsonColumnQualifier HBase column qualifier in which json responses are stored
     * @return a mapper from a key geocode responses into HBase Puts
     */
    public static BiFunction<byte[], Collection<GeocodeResponse>, Put> valueMutator(byte[] columnFamily,
                                                                                     byte[] countryCodeColumnQualifier,
                                                                                     byte[] jsonColumnQualifier) {
        return (key, geocodeResponses) -> {
            try {
                Put put = new Put(key);
                put.addColumn(columnFamily, countryCodeColumnQualifier, countryCodeMapper().apply(geocodeResponses).getBytes());
                put.addColumn(columnFamily, jsonColumnQualifier, MAPPER.writeValueAsBytes(geocodeResponses));
                return put;
            } catch (IOException ex) {
                LOG.error("Error serializing response into bytes", ex);
                throw new RuntimeException(ex);
            }
        };
    }

    /**
     * Create a new instance of a Geocode KV store/cache backed by an HBase table.
     * This instance only returns the ISO country code of the resulting geocode lookup.
     * @param configuration KV store configuration
     * @return a new instance of Geocode KV store
     * @throws IOException if the Rest client can't be created
     */
    public static KeyValueStore<LatLng, String> simpleGeocodeKVStore(GeocodeKVStoreConfiguration configuration) throws IOException {
        GeocodeService geocodeService = GeocodeServiceFactory.createGeocodeServiceClient(configuration.getGeocodeClientConfig());
        return HBaseStore.<LatLng, String, Collection<GeocodeResponse>>builder()
                .withHBaseStoreConfiguration(configuration.getHBaseKVStoreConfiguration())
                .withResultMapper(simpleResultMapper(configuration.getHBaseKVStoreConfiguration().getColumnFamily().getBytes(),
                                                     configuration.getCountryCodeColumnQualifier().getBytes()))
                .withValueMutator(valueMutator(configuration.getHBaseKVStoreConfiguration().getColumnFamily().getBytes(),
                                               configuration.getCountryCodeColumnQualifier().getBytes(),
                                               configuration.getJsonColumnQualifier().getBytes()))
                .withLoader(latLng -> {
                    try {
                        Response<Collection<GeocodeResponse>> response = geocodeService.reverse(latLng.getLatitude(), latLng.getLongitude()).execute();
                        if (response.isSuccessful() && Objects.nonNull(response.body()) && !response.body().isEmpty()) {
                            return response.body();
                        }
                    } catch (IOException ex) {
                        LOG.error("Error contacting geocode service", ex);
                        throw new IllegalStateException(ex);
                    }
                    return null;

                })
                .build();

    }

}
