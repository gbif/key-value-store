package org.gbif.kvs.geocode;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.gbif.kvs.KeyValueStore;
import org.gbif.kvs.SaltedKeyGenerator;
import org.gbif.rest.client.geocode.GeocodeResponse;
import org.gbif.rest.client.geocode.Location;
import org.gbif.rest.client.geocode.test.GeocodeTestService;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.Objects;
import java.util.Optional;

import org.apache.hadoop.hbase.HBaseTestingUtility;
import org.apache.hadoop.hbase.client.Get;
import org.apache.hadoop.hbase.client.HTable;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.util.Bytes;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

@RunWith(Parameterized.class)
public class GeocodeKVHBaseStoreTestIT {

  //-- Static elements shared for all tests

  private static TestConfiguration testConfiguration;

  private static HBaseTestingUtility utility;

  private static HTable geocodeKvTable;

  private static KeyValueStore<LatLng, GeocodeResponse> geocodeKeyValueStore;

  // Used to store and retrieve JSON values stored in HBase
  private static final ObjectMapper MAPPER = new ObjectMapper();

  static {
    MAPPER.setSerializationInclusion(JsonInclude.Include.NON_NULL);
    MAPPER.enable(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY);
  }

  //-- End of shared elements

  //--- Elements of parameterized tests
  private final LatLng latLng;

  private final GeocodeResponse geocodeResponse;

  //-- End of parameterized tests


  @Parameterized.Parameters(name = "{index}: Lookup({0})=Country({1})")
  public static Collection<Object[]> data() {
    return TestCoordinates.COORDINATES;
  }

  /**
   * Creates an instance using the test data.
   * @param latLng coordinate to test
   * @param countryCode expected country code
   */
  public GeocodeKVHBaseStoreTestIT(LatLng latLng, String countryCode) {
    this.latLng = latLng;
    this.geocodeResponse = Optional.ofNullable(countryCode).map( isoCode -> {
                              Location location = new Location();
                              location.setIsoCountryCode2Digit(countryCode);
                              GeocodeResponse geocodeResponse = new GeocodeResponse();
                              geocodeResponse.setLocations(Collections.singletonList(location));
                              return geocodeResponse;
                            })
            .orElse(null);
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
  private static KeyValueStore<LatLng, GeocodeResponse> geocodeKeyValueStore() throws IOException {
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
    Assert.assertEquals("Country stored is different",  Objects.isNull(geocodeResponse), result.isEmpty());
    GeocodeResponse hGeocodeResponse =
    Optional.ofNullable(Bytes.toString(result.getValue(Bytes.toBytes(testConfiguration.getHBaseKVStoreConfiguration().getColumnFamily()),
            Bytes.toBytes(testConfiguration.getGeocodeKVStoreConfiguration().getJsonColumnQualifier())))).map(val -> {
              try {
                return MAPPER.readValue(val, GeocodeResponse.class);
              } catch (IOException ex) {
                throw new RuntimeException(ex);
              }
            }).orElse(null);
    Assert.assertTrue(assertSameContent(geocodeResponse, hGeocodeResponse));
  }

  private boolean assertSameContent(GeocodeResponse response1, GeocodeResponse response2) {
    return (Objects.isNull(response1) && Objects.isNull(response2)) ||
            response1.getLocations().stream().allMatch(location -> response2.getLocations().stream()
            .anyMatch( expectedLocation -> expectedLocation.getIsoCountryCode2Digit().equals(location.getIsoCountryCode2Digit())));
  }

  /**
   * Test that a coordinate is created in HBase and it can be later retrieved from the table.
   */
  @Test
  public void getAndInsertTest() throws IOException {
    GeocodeResponse response = geocodeKeyValueStore.get(latLng);
    Assert.assertTrue((Objects.isNull(response) && Objects.isNull(geocodeResponse)) ||
                      response.getLocations().stream().anyMatch(location -> geocodeResponse.getLocations().stream()
                              .anyMatch( expectedLocation -> expectedLocation.getIsoCountryCode2Digit().equals(location.getIsoCountryCode2Digit()))));
    assertIsInHBase();
  }

}
