package org.gbif.kvs.species;

import org.gbif.kvs.hbase.HBaseKVStoreConfiguration;

import java.io.Serializable;

/** Configuration settings to create a KV Store/Cache for the GBIF species name match. */
public class NameUsageMatchKVConfiguration implements Serializable {

  // HBase KV store configuration
  private final HBaseKVStoreConfiguration hBaseKVStoreConfiguration;

  // Stores the entire JSON response of the Geocode service
  private final String jsonColumnQualifier;


  /**
   * Creates an configuration instance using the HBase KV and Rest client configurations.
   *
   * @param hBaseKVStoreConfiguration HBase KV store configuration
   * @param jsonColumnQualifier column qualifier to store the entire json response
   */
  public NameUsageMatchKVConfiguration(HBaseKVStoreConfiguration hBaseKVStoreConfiguration, String jsonColumnQualifier) {
    this.hBaseKVStoreConfiguration = hBaseKVStoreConfiguration;
    this.jsonColumnQualifier = jsonColumnQualifier;
  }

  /** @return HBase KV store configuration */
  public HBaseKVStoreConfiguration getHBaseKVStoreConfiguration() {
    return hBaseKVStoreConfiguration;
  }


  /** @return JSON response column qualifier */
  public String getJsonColumnQualifier() {
    return jsonColumnQualifier;
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

    private String jsonColumnQualifier;


    /**
     * Hidden constructor to force use the containing class builder() method.
     */
    private Builder() {
      //DO NOTHING
    }

    public Builder withHBaseKVStoreConfiguration(
        HBaseKVStoreConfiguration hBaseKVStoreConfiguration) {
      this.hBaseKVStoreConfiguration = hBaseKVStoreConfiguration;
      return this;
    }

    public Builder withJsonColumnQualifier(String jsonColumnQualifier) {
      this.jsonColumnQualifier = jsonColumnQualifier;
      return this;
    }

    public NameUsageMatchKVConfiguration build() {
      return new NameUsageMatchKVConfiguration(hBaseKVStoreConfiguration, jsonColumnQualifier);
    }

  }
}
