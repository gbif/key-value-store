package org.gbif.kvs.geocode;

import org.gbif.kvs.hbase.HBaseKVStoreConfiguration;

import java.io.Serializable;

/** Configuration settings to create a KV Store/Cache for the GBIF reverse geocode service. */
public class GeocodeKVStoreConfiguration implements Serializable {

  // HBase KV store configuration
  private final HBaseKVStoreConfiguration hBaseKVStoreConfiguration;

  // Column qualifier to store the preferred country code
  private final String countryCodeColumnQualifier;

  // Stores the entire JSON response of the Geocode service
  private final String jsonColumnQualifier;

  /** @return HBase KV store configuration */
  public HBaseKVStoreConfiguration getHBaseKVStoreConfiguration() {
    return hBaseKVStoreConfiguration;
  }

  /**
   * Creates an configuration instance using the HBase KV and Rest client configurations.
   *
   * @param hBaseKVStoreConfiguration HBase KV store configuration
   * @param countryCodeColumnQualifier ISO country code column qualifier
   * @param jsonColumnQualifier column qualifier to store the entire json response
   */
  public GeocodeKVStoreConfiguration(HBaseKVStoreConfiguration hBaseKVStoreConfiguration,
                                     String countryCodeColumnQualifier,
                                     String jsonColumnQualifier) {
    this.hBaseKVStoreConfiguration = hBaseKVStoreConfiguration;
    this.countryCodeColumnQualifier = countryCodeColumnQualifier;
    this.jsonColumnQualifier = jsonColumnQualifier;
  }

  /** @return ISO country code column qualifier */
  public String getCountryCodeColumnQualifier() {
    return countryCodeColumnQualifier;
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

    private String countryCodeColumnQualifier;

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

    public Builder withCountryCodeColumnQualifier(String countryCodeColumnQualifier) {
      this.countryCodeColumnQualifier = countryCodeColumnQualifier;
      return this;
    }

    public Builder withJsonColumnQualifier(String jsonColumnQualifier) {
      this.jsonColumnQualifier = jsonColumnQualifier;
      return this;
    }


    public GeocodeKVStoreConfiguration build() {
      return new GeocodeKVStoreConfiguration(
          hBaseKVStoreConfiguration, countryCodeColumnQualifier,jsonColumnQualifier);
    }
  }
}
