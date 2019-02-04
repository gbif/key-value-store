package org.gbif.kvs.geocode;

import org.gbif.kvs.KeyValueStore;
import org.gbif.kvs.hbase.HBaseStore;
import org.gbif.rest.client.geocode.GeocodeResponse;
import org.gbif.rest.client.geocode.GeocodeService;
import org.gbif.rest.client.geocode.GeocodeServiceFactory;

import java.io.IOException;
import java.util.Collection;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import retrofit2.Response;

/**
 *
 */
public class GeocodeKVStoreFactory {

    private static Logger LOG = LoggerFactory.getLogger(GeocodeServiceFactory.class);

    /**
     * Hidden constructor.
     */
    private GeocodeKVStoreFactory() {
        //DO NOTHING
    }

    /**
     * Create a new instance of a Geocode KV store/cache backed by an HBase table.
     * @param configuration KV store configuration
     * @return a new instance of Geocode KV store
     * @throws IOException if the Rest client can't be created
     */
    public KeyValueStore<LatLng, String> geocodeKVStore(GeocodeKVStoreConfiguration configuration) throws IOException {
        GeocodeService geocodeService = GeocodeServiceFactory.createGeocodeServiceClient(configuration.getGeocodeClientConfig());
        return HBaseStore.<LatLng, String>builder()
                .withHBaseStoreConfiguration(configuration.getHBaseKVStoreConfiguration())
                .withValueDeSerializer(String::new)
                .withValueSerializer(String::getBytes)
                .withLoader(latLng -> {
                    try {
                        Response<Collection<GeocodeResponse>> response = geocodeService.reverse(latLng.getLatitude(), latLng.getLongitude()).execute();
                        if (response.isSuccessful() && Objects.nonNull(response.body())) {
                            GeocodeResponse firstCountry = response.body().iterator().next();
                            return firstCountry.getIsoCountryCode2Digit();
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
