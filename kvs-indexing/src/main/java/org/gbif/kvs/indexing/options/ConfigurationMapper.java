package org.gbif.kvs.indexing.options;

import org.gbif.kvs.hbase.HBaseKVStoreConfiguration;
import org.gbif.rest.client.configuration.ClientConfiguration;

/** Converts common Beam pipeline options into configuration classes. */
public class ConfigurationMapper {

  /** Hidden constructor. */
  private ConfigurationMapper() {
    // DO NOTHING
  }

  /**
   * Creates a {@link HBaseKVStoreConfiguration} from a {@link HBaseIndexingOptions} instance.
   *
   * @param options pipeline options
   * @return a new instance of CachedHBaseKVStoreConfiguration
   */
  public static HBaseKVStoreConfiguration hbaseKVStoreConfiguration(HBaseIndexingOptions options) {
    return HBaseKVStoreConfiguration.builder()
            .withTableName(options.getTargetTable())
            .withColumnFamily(options.getKVColumnFamily())
            .withHBaseZk(options.getHbaseZk())
            .withNumOfKeyBuckets(options.getSaltedKeyBuckets())
            .build();
  }

  /**
   * Creates a {@link ClientConfiguration} from a {@link HBaseIndexingOptions} instance.
   *
   * @param options pipeline options
   * @return a new instance of ClientConfiguration
   */
  public static ClientConfiguration clientConfiguration(HBaseIndexingOptions options) {
    return ClientConfiguration.builder()
            .withBaseApiUrl(options.getBaseApiUrl())
            .withTimeOut(options.getApiTimeOut())
            .withFileCacheMaxSizeMb(options.getRestClientCacheMaxSize())
            .build();
  }
}
