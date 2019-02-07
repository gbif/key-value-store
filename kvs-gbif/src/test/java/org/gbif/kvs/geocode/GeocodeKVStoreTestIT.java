package org.gbif.kvs.geocode;

import org.gbif.kvs.KeyValueStore;
import org.gbif.kvs.hbase.HBaseKVStoreConfiguration;
import org.gbif.rest.client.configuration.ClientConfiguration;

import java.io.IOException;
import java.util.Objects;

import org.apache.hadoop.hbase.HBaseTestingUtility;
import org.apache.hadoop.hbase.client.HTable;
import org.apache.hadoop.hbase.util.Bytes;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

@Ignore
public class GeocodeKVStoreTestIT {

  private static HBaseTestingUtility utility;

  private static HTable geocodeKvTable;

  private static HBaseKVStoreConfiguration hBaseKVStoreConfiguration;

  private static KeyValueStore<LatLng,String> geocodeKeyValueStore;


  private final static HBaseKVStoreConfiguration.Builder HBASE_KV_STORE_CONFIGURATION = HBaseKVStoreConfiguration.builder()
                                                                                  .withTableName("geocode_kv")
                                                                                  .withNumOfKeyBuckets(4)
                                                                                  .withColumnFamily("v");


  private static HBaseKVStoreConfiguration hBaseKVStoreConfiguration() {
    if (Objects.isNull(hBaseKVStoreConfiguration)) {
      hBaseKVStoreConfiguration = HBASE_KV_STORE_CONFIGURATION
                                    .withHBaseZk("localhost:" + utility.getZkCluster().getClientPort())
                                    .build();
    }
    return  hBaseKVStoreConfiguration;
  }

  private static HTable createTable() throws IOException {
    return utility.createTable(Bytes.toBytes(hBaseKVStoreConfiguration().getTableName()),
                               Bytes.toBytes(hBaseKVStoreConfiguration().getColumnFamily()));
  }

  private static KeyValueStore<LatLng,String> geocodeKeyValueStore() throws IOException {
    HBaseKVStoreConfiguration hBaseKVStoreConfiguration = hBaseKVStoreConfiguration();
    return GeocodeKVStoreFactory.simpleGeocodeKVStore(GeocodeKVStoreConfiguration.builder()
            .withJsonColumnQualifier("j") //stores JSON data
            .withCountryCodeColumnQualifier("c") //stores ISO country code
            .withHBaseKVStoreConfiguration(hBaseKVStoreConfiguration).build(),
           ClientConfiguration.builder()
                .withBaseApiUrl("https://api.gbif.org/v1/") //GBIF base API url
                .withFileCacheMaxSizeMb(64L) //Max file cache size
                .withTimeOut(60L) //Geocode service connection time-out
                .build());
  }

  private static KeyValueStore<LatLng,String> geocodeKvStore;

  @Before
  public void setup() throws Exception {
    utility = new HBaseTestingUtility();
    utility.startMiniCluster();
    geocodeKvTable = createTable();
    geocodeKeyValueStore = geocodeKeyValueStore();
  }

  @After
  public void tearDown() throws Exception {
    if (Objects.nonNull(geocodeKvTable)) {
      geocodeKvTable.close();
    }
    if (Objects.nonNull(utility)) {
      utility.shutdownMiniCluster();
    }
  }


  @Test
  public void insertTest() {
    String countryCode = geocodeKeyValueStore.get(LatLng.builder().withLatitude(45.0).withLongitude(75.8).build());
    Assert.assertEquals("KZ", countryCode);
  }

}
