package org.gbif.kvs.geocode;

import org.gbif.kvs.hbase.HBaseKVStoreConfiguration;
import org.gbif.rest.client.config.ClientConfig;

/**
 * Configuration settings to create a KV Store/Cache for the GBIF reverse geocode service.
 */
public class GeocodeKVStoreConfiguration {

    //HBase KV store configuration
    private final HBaseKVStoreConfiguration hBaseKVStoreConfiguration;

    //Rest Geocode client configuration
    private final ClientConfig geocodeClientConfig;

    /**
     * Creates an config instance using the HBase KV and Rest client configurations.
     * @param hBaseKVStoreConfiguration HBase KV store config
     * @param geocodeClientConfig Geocode REST client config
     */
    public GeocodeKVStoreConfiguration(HBaseKVStoreConfiguration hBaseKVStoreConfiguration,
                                       ClientConfig geocodeClientConfig) {
        this.hBaseKVStoreConfiguration = hBaseKVStoreConfiguration;
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
     * Builder for the Geocode KV store/cache configuration.
     */
    public static class Builder {

        private HBaseKVStoreConfiguration hBaseKVStoreConfiguration;

        private ClientConfig geocodeClientConfig;

        public Builder setHBaseKVStoreConfiguration(HBaseKVStoreConfiguration hBaseKVStoreConfiguration) {
            this.hBaseKVStoreConfiguration = hBaseKVStoreConfiguration;
            return this;
        }

        public Builder setGeocodeClientConfig(ClientConfig geocodeClientConfig) {
            this.geocodeClientConfig = geocodeClientConfig;
            return this;
        }

        public GeocodeKVStoreConfiguration createConfiguration() {
            return new GeocodeKVStoreConfiguration(hBaseKVStoreConfiguration, geocodeClientConfig);
        }
    }
}
