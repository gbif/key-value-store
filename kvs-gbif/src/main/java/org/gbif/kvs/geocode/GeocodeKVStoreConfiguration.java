package org.gbif.kvs.geocode;

import org.gbif.kvs.hbase.HBaseKVStoreConfiguration;
import org.gbif.rest.client.config.ClientConfig;

import java.io.Serializable;

/**
 * Configuration settings to create a KV Store/Cache for the GBIF reverse geocode service.
 */
public class GeocodeKVStoreConfiguration implements Serializable {


    //HBase KV store configuration
    private final HBaseKVStoreConfiguration hBaseKVStoreConfiguration;


    //Column qualifier to store the preferred country code
    private final String countryCodeColumnQualifier;

    //Stores the entire JSON response of the Geocode service
    private final String jsonColumnQualifier;

    //Rest Geocode client configuration
    private final ClientConfig geocodeClientConfig;

    /**
     * Creates an config instance using the HBase KV and Rest client configurations.
     * @param hBaseKVStoreConfiguration HBase KV store config
     * @param countryCodeColumnQualifier ISO country code column qualifier
     * @param jsonColumnQualifier column qualifier to store the entire json response
     * @param geocodeClientConfig Geocode REST client config
     */
    public GeocodeKVStoreConfiguration(HBaseKVStoreConfiguration hBaseKVStoreConfiguration,
                                       String countryCodeColumnQualifier,
                                       String jsonColumnQualifier,
                                       ClientConfig geocodeClientConfig) {
        this.hBaseKVStoreConfiguration = hBaseKVStoreConfiguration;
        this.countryCodeColumnQualifier = countryCodeColumnQualifier;
        this.jsonColumnQualifier = jsonColumnQualifier;
        this.geocodeClientConfig = geocodeClientConfig;
    }

    /**
     *
     * @return HBase KV store config
     */
    public HBaseKVStoreConfiguration getHBaseKVStoreConfiguration() {
        return hBaseKVStoreConfiguration;
    }

    /**
     *
     * @return Geocode REST client config
     */
    public ClientConfig getGeocodeClientConfig() {
        return geocodeClientConfig;
    }


    /**
     *
     * @return ISO country code column qualifier
     */
    public String getCountryCodeColumnQualifier() {
        return countryCodeColumnQualifier;
    }

    /**
     *
     * @return JSON response column qualifier
     */
    public String getJsonColumnQualifier() {
        return jsonColumnQualifier;
    }

    /**
     * Builder for the Geocode KV store/cache configuration.
     */
    public static class Builder {

        private HBaseKVStoreConfiguration hBaseKVStoreConfiguration;

        private String countryCodeColumnQualifier;

        private String jsonColumnQualifier;

        private ClientConfig geocodeClientConfig;

        public Builder withHBaseKVStoreConfiguration(HBaseKVStoreConfiguration hBaseKVStoreConfiguration) {
            this.hBaseKVStoreConfiguration = hBaseKVStoreConfiguration;
            return this;
        }

        public Builder withCountryCodeColumnQualifier(String countryCodeColumnQualifier) {
            this.countryCodeColumnQualifier = countryCodeColumnQualifier;
            return this;
        }

        public Builder withJsonColumnQualifier(String jsonColumnQualifier) {
            this.jsonColumnQualifier = jsonColumnQualifier;
            return this;
        }

        public Builder withGeocodeClientConfig(ClientConfig geocodeClientConfig) {
            this.geocodeClientConfig = geocodeClientConfig;
            return this;
        }

        public GeocodeKVStoreConfiguration build() {
            return new GeocodeKVStoreConfiguration(hBaseKVStoreConfiguration, countryCodeColumnQualifier,
                                                   jsonColumnQualifier, geocodeClientConfig);
        }
    }
}
