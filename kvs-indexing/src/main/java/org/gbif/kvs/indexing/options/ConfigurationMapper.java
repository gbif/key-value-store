package org.gbif.kvs.indexing.options;

import org.gbif.kvs.hbase.HBaseKVStoreConfiguration;
import org.gbif.rest.client.config.ClientConfig;

/**
 * Converts common Beam pipeline options into configuration classes.
 */
public class ConfigurationMapper {

    /**
     * Hidden constructor.
     */
    private ConfigurationMapper() {
        //DO NOTHING
    }

    /**
     * Creates a {@link HBaseKVStoreConfiguration} from a {@link HBaseIndexingOptions} instance.
     * @param options pipeline options
     * @return a new instance of HBaseKVStoreConfiguration
     */
    public static HBaseKVStoreConfiguration hbaseKVStoreConfiguration(HBaseIndexingOptions options) {
        return new HBaseKVStoreConfiguration.Builder()
                .withTableName(options.getTargetTable())
                .withColumnFamily(options.getKVColumnFamily())
                .withHBaseZk(options.getHbaseZk())
                .withNumOfKeyBuckets(options.getSaltedKeyBuckets())
                .build();
    }


    /**
     * Creates a {@link ClientConfig} from a {@link HBaseIndexingOptions} instance.
     * @param options pipeline options
     * @return a new instance of ClientConfig
     */
    public static ClientConfig clientConfig(HBaseIndexingOptions options) {
        return new ClientConfig.Builder()
                .withBaseApiUrl(options.getBaseApiUrl())
                .withTimeOut(options.getApiTimeOut())
                .withFileCacheMaxSizeMb(options.getRestClientCacheMaxSize())
                .build();
    }
}
