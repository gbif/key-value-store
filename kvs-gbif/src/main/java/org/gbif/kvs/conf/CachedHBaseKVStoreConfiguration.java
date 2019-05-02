package org.gbif.kvs.conf;

import java.io.Serializable;

/** Configuration settings to create a KV Store/Cache for the GBIF species name match. */
public class CachedHBaseKVStoreConfiguration implements Serializable {

  // HBase KV store configuration
  private final org.gbif.kvs.hbase.HBaseKVStoreConfiguration hBaseKVStoreConfiguration;

  // Stores the entire JSON response of the Geocode service
  private final String valueColumnQualifier;


  private final Long cacheCapacity;


  /**
   * Creates an configuration instance using the HBase KV and Rest client configurations.
   *
   * @param hBaseKVStoreConfiguration HBase KV store configuration
   * @param valueColumnQualifier column qualifier to store the entire json response
   * @param cacheCapacity maximum number of entries in the in-memory cache
   */
  public CachedHBaseKVStoreConfiguration(org.gbif.kvs.hbase.HBaseKVStoreConfiguration hBaseKVStoreConfiguration, String valueColumnQualifier, Long cacheCapacity) {
    this.hBaseKVStoreConfiguration = hBaseKVStoreConfiguration;
    this.valueColumnQualifier = valueColumnQualifier;
    this.cacheCapacity = cacheCapacity;
  }

  /** @return HBase KV store configuration */
  public org.gbif.kvs.hbase.HBaseKVStoreConfiguration getHBaseKVStoreConfiguration() {
    return hBaseKVStoreConfiguration;
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

    private org.gbif.kvs.hbase.HBaseKVStoreConfiguration hBaseKVStoreConfiguration;

    private String valueColumnQualifier;

    private Long cacheCapacity;


    /**
     * Hidden constructor to force use the containing class builder() method.
     */
    private Builder() {
      //DO NOTHING
    }

    public Builder withHBaseKVStoreConfiguration(org.gbif.kvs.hbase.HBaseKVStoreConfiguration hBaseKVStoreConfiguration) {
      this.hBaseKVStoreConfiguration = hBaseKVStoreConfiguration;
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
      return new CachedHBaseKVStoreConfiguration(hBaseKVStoreConfiguration, valueColumnQualifier, cacheCapacity);
    }

  }
}
