package org.gbif.kvs.geocode;

import org.gbif.kvs.hbase.HBaseKVStoreConfiguration;

/**
 * Class holder for default configuration values used for testing.
 *   - HBase table name: geocode_kv
 *   - Number of key buckets: 4
 *   - Column Family: v
 *   - Country code column family qualifier: c
 *   - JSON response column family qualifier: j
 */
class TestConfiguration {

  //Generated configuration
  private final GeocodeKVStoreConfiguration geocodeKVStoreConfiguration;

  //Pre-built builder
  private final static HBaseKVStoreConfiguration.Builder HBASE_KV_STORE_CONFIGURATION = HBaseKVStoreConfiguration.builder()
      .withTableName("geocode_kv")
      .withNumOfKeyBuckets(4)
      .withColumnFamily("v")
      .withCacheCapacity(2L); //Cache size of 2

  /**
   * Creates a test configuration using default test value and the ZK port assign by the mini cluster.
   * @param zkClientPort Zookeeper port assigned by the test framework
   */
  TestConfiguration(int zkClientPort) {

    geocodeKVStoreConfiguration = GeocodeKVStoreConfiguration.builder()
                                    .withJsonColumnQualifier("j") //stores JSON data
                                    .withCountryCodeColumnQualifier("c") //stores ISO country code
                                    .withHBaseKVStoreConfiguration(HBASE_KV_STORE_CONFIGURATION
                                                                    .withHBaseZk("localhost:" + zkClientPort)
                                                                    .build())
                                    .build();
  }

  /**
   *
   * @return test geocode KV store configuration
   */
  GeocodeKVStoreConfiguration getGeocodeKVStoreConfiguration() {
    return geocodeKVStoreConfiguration;
  }

  /**
   *
   * @return test HBase KV store configuration
   */
  HBaseKVStoreConfiguration getHBaseKVStoreConfiguration() {
    return geocodeKVStoreConfiguration.getHBaseKVStoreConfiguration();
  }
}
