package org.gbif.kvs.geocode;

import org.gbif.kvs.KeyValueStore;
import org.gbif.kvs.SaltedKeyGenerator;
import org.gbif.rest.client.geocode.test.GeocodeTestService;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Objects;

import org.apache.hadoop.hbase.HBaseTestingUtility;
import org.apache.hadoop.hbase.client.Get;
import org.apache.hadoop.hbase.client.HTable;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.Table;
import org.apache.hadoop.hbase.util.Bytes;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

@RunWith(Parameterized.class)
public class GeocodeKVStoreTestIT {

  //-- Static elements shared for all tests

  private static TestConfiguration testConfiguration;

  private static HBaseTestingUtility utility;

  private static HTable geocodeKvTable;

  private static KeyValueStore<LatLng,String> geocodeKeyValueStore;

  //-- End of shared elements

  //--- Elements of parameterized tests
  private final LatLng latLng;

  private final String countryCode;

  //-- End of parameterized tests


  @Parameterized.Parameters(name = "{index}: Lookup({0})=Country({1})")
  public static Collection<Object[]> data() {
    return Arrays.asList(new Object[][] {
        { LatLng.create(48.019573, 66.923684), "KZ" },
        { LatLng.create(35.937496, 14.375416), "MT" },
        { LatLng.create(-16.290154, -63.588653), "BO" },
        { LatLng.create(36.93, 13.37), null }
    });
  }

  /**
   * Creates an instance using the test data.
   * @param latLng coordinate to test
   * @param countryCode expected country code
   */
  public GeocodeKVStoreTestIT(LatLng latLng, String countryCode) {
    this.latLng = latLng;
    this.countryCode = countryCode;
  }


  /**
   *
   * @return a new HBase table
   * @throws IOException in case of error creating the table
   */
  private static HTable createTable() throws IOException {
    return utility.createTable(Bytes.toBytes(testConfiguration.getHBaseKVStoreConfiguration().getTableName()),
                               Bytes.toBytes(testConfiguration.getHBaseKVStoreConfiguration().getColumnFamily()));
  }

  /**
   * Creates a Geocode KV store using a test {@link GeocodeTestService}.
   * @return a new Geocode KV store
   * @throws IOException if something went wrong creating the store
   */
  private static KeyValueStore<LatLng,String> geocodeKeyValueStore() throws IOException {
    return GeocodeKVStoreFactory.simpleGeocodeKVStore(testConfiguration.getGeocodeKVStoreConfiguration(),
                                                      new GeocodeTestService());
  }

  @BeforeClass
  public static void setup() throws Exception {
    utility = new HBaseTestingUtility();
    utility.startMiniCluster();
    testConfiguration = new TestConfiguration(utility.getZkCluster().getClientPort());
    geocodeKvTable = createTable();
    geocodeKeyValueStore = geocodeKeyValueStore();
  }

  @AfterClass
  public static void tearDown() throws Exception {
    if (Objects.nonNull(geocodeKvTable)) {
      geocodeKvTable.close();
    }
    if (Objects.nonNull(utility)) {
      utility.shutdownMiniCluster();
    }
    if (Objects.nonNull(geocodeKeyValueStore)) {
      geocodeKeyValueStore.close();
    }
  }


  /**
   * Is the LatLng present in HBase.
   */
  private void assertIsInHBase() throws IOException {
    SaltedKeyGenerator saltedKeyGenerator = new SaltedKeyGenerator(testConfiguration.getHBaseKVStoreConfiguration().getNumOfKeyBuckets());
    Get get = new Get(saltedKeyGenerator.computeKey(latLng.getLogicalKey()));
    Result result = geocodeKvTable.get(get);
    Assert.assertEquals("Country stored is different",  Objects.isNull(countryCode), result.isEmpty());
    String hCountryCode = Bytes.toString(result.getValue(Bytes.toBytes(testConfiguration.getHBaseKVStoreConfiguration().getColumnFamily()),
                                                         Bytes.toBytes(testConfiguration.getGeocodeKVStoreConfiguration().getCountryCodeColumnQualifier())));
    Assert.assertEquals(countryCode, hCountryCode);
  }

  /**
   * Test that a coordinate is created in HBase and it can be later retrieved from the table.
   */
  @Test
  public void getAndInsertTest() throws IOException {
    String storedCountryCode = geocodeKeyValueStore.get(latLng);
    Assert.assertEquals(countryCode, storedCountryCode);
    assertIsInHBase();
  }

}
