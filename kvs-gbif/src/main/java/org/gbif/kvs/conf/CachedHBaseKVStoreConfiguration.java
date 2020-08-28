package org.gbif.kvs.conf;

import org.gbif.kvs.hbase.HBaseKVStoreConfiguration;
import org.gbif.kvs.hbase.LoaderRetryConfig;

import java.io.Serializable;

import lombok.Builder;
import lombok.Getter;

/** Configuration settings to create a KV Store/Cache for the GBIF species name match. */
@Getter
@Builder(setterPrefix = "with")
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
}
