/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.gbif.kvs.geocode;

import org.gbif.kvs.conf.CachedHBaseKVStoreConfiguration;
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
  private final CachedHBaseKVStoreConfiguration geocodeKVStoreConfiguration;

  //Pre-built builder
  private final static HBaseKVStoreConfiguration.Builder HBASE_KV_STORE_CONFIGURATION = HBaseKVStoreConfiguration.builder()
      .withTableName("geocode_kv")
      .withNumOfKeyBuckets(4)
      .withColumnFamily("v");

  /**
   * Creates a test configuration using default test value and the ZK port assign by the mini cluster.
   * @param zkClientPort Zookeeper port assigned by the test framework
   */
  TestConfiguration(int zkClientPort) {

    geocodeKVStoreConfiguration = CachedHBaseKVStoreConfiguration.builder()
                                    .withValueColumnQualifier("j") //stores JSON data
                                    .withHBaseKVStoreConfiguration(HBASE_KV_STORE_CONFIGURATION
                                                                    .withHBaseZk("localhost:" + zkClientPort)
                                                                    .build())
                                    .withCacheCapacity(2L) //Cache size of 2
                                    .build();
  }

  /**
   *
   * @return test geocode KV store configuration
   */
  CachedHBaseKVStoreConfiguration getGeocodeKVStoreConfiguration() {
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
