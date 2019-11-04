package org.gbif.kvs.conf;

import org.gbif.kvs.hbase.HBaseKVStoreConfiguration;
import org.gbif.kvs.hbase.LoaderRetryConfig;

import java.io.Serializable;

/** Configuration settings to create a KV Store/Cache for the GBIF species name match. */
public class CachedHBaseKVStoreConfiguration implements Serializable {

  // HBase KV store configuration
  private final HBaseKVStoreConfiguration hBaseKVStoreConfiguration;

  //Exponential backoff loader retry config
  private final LoaderRetryConfig loaderRetryConfig;

  // Stores the entire JSON response of the Geocode service
  private final String valueColumnQualifier;


  private final Long cacheCapacity;


  /**
   * Creates an configuration instance using the HBase KV and Rest client configurations.
   *
   * @param hBaseKVStoreConfiguration HBase KV store configuration
   * @param loaderRetryConfig exponential backoff retry config
   * @param valueColumnQualifier column qualifier to store the entire json response
   * @param cacheCapacity maximum number of entries in the in-memory cache
   */
  public CachedHBaseKVStoreConfiguration(HBaseKVStoreConfiguration hBaseKVStoreConfiguration, LoaderRetryConfig loaderRetryConfig,
                                         String valueColumnQualifier, Long cacheCapacity) {
    this.hBaseKVStoreConfiguration = hBaseKVStoreConfiguration;
    this.loaderRetryConfig = loaderRetryConfig;
    this.valueColumnQualifier = valueColumnQualifier;
    this.cacheCapacity = cacheCapacity;
  }

  /** @return HBase KV store configuration */
  public HBaseKVStoreConfiguration getHBaseKVStoreConfiguration() {
    return hBaseKVStoreConfiguration;
  }

  /** @return backoff exponential retry config */
  public LoaderRetryConfig getLoaderRetryConfig() {
    return loaderRetryConfig;
  }

  /** @return JSON response column qualifier */
  public String getValueColumnQualifier() {
    return valueColumnQualifier;
  }


  /**
   * Maximum number of entries in the in-memory cache.
   * @return the maximum cache capacity
   */
  public Long getCacheCapacity() {
    return cacheCapacity;
  }
  /**
   * Creates a new {@link Builder} instance.
   * @return a new builder
   */
  public static Builder builder() {
    return new Builder();
  }

  /** Builder for the Geocode KV store/cache configuration. */
  public static class Builder {

    private HBaseKVStoreConfiguration hBaseKVStoreConfiguration;

    private LoaderRetryConfig loaderRetryConfig;


    private String valueColumnQualifier;

    private Long cacheCapacity;


    /**
     * Hidden constructor to force use the containing class builder() method.
     */
    private Builder() {
      //DO NOTHING
    }

    public Builder withHBaseKVStoreConfiguration(HBaseKVStoreConfiguration hBaseKVStoreConfiguration) {
      this.hBaseKVStoreConfiguration = hBaseKVStoreConfiguration;
      return this;
    }

    public Builder withLoaderRetryConfiguration(LoaderRetryConfig loaderRetryConfig) {
      this.loaderRetryConfig = loaderRetryConfig;
      return this;
    }

    public Builder withValueColumnQualifier(String valueColumnQualifier) {
      this.valueColumnQualifier = valueColumnQualifier;
      return this;
    }

    public Builder withCacheCapacity(Long cacheCapacity) {
      this.cacheCapacity = cacheCapacity;
      return this;
    }

    public CachedHBaseKVStoreConfiguration build() {
      return new CachedHBaseKVStoreConfiguration(hBaseKVStoreConfiguration, loaderRetryConfig, valueColumnQualifier, cacheCapacity);
    }

  }
}
