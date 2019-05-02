package org.gbif.kvs.geocode;

import org.gbif.kvs.KeyValueStore;
import org.gbif.kvs.conf.CachedHBaseKVStoreConfiguration;
import org.gbif.rest.client.geocode.GeocodeResponse;
import org.gbif.rest.client.geocode.test.GeocodeTestService;

import java.io.IOException;
import java.util.Collection;
import java.util.Objects;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

@RunWith(Parameterized.class)
public class GeocodeKVServiceStoreTestIT {

  //-- Static elements shared for all tests

  private static KeyValueStore<LatLng,GeocodeResponse> geocodeKeyValueStore;

  //-- End of shared elements

  //--- Elements of parameterized tests
  private final LatLng latLng;

  private final String countryCode;

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
  public GeocodeKVServiceStoreTestIT(LatLng latLng, String countryCode) {
    this.latLng = latLng;
    this.countryCode = countryCode;
  }

  /**
   * Creates a Geocode KV store using a test {@link GeocodeTestService}.
   * @return a new Geocode KV store
   * @throws IOException if something went wrong creating the store
   */
  private static KeyValueStore<LatLng, GeocodeResponse> geocodeKeyValueStore() throws IOException {
    return GeocodeKVStoreFactory.simpleGeocodeKVStore(CachedHBaseKVStoreConfiguration.builder().build(),
                                                      new GeocodeTestService());
  }

  @BeforeClass
  public static void setup() throws Exception {
    geocodeKeyValueStore = geocodeKeyValueStore();
  }

  @AfterClass
  public static void tearDown() throws Exception {
    if (Objects.nonNull(geocodeKeyValueStore)) {
      geocodeKeyValueStore.close();
    }
  }



  /**
   * Test that a coordinate exists and it can be later retrieved from the service.
   */
  @Test
  public void getTest() {
    GeocodeResponse response = geocodeKeyValueStore.get(latLng);
    Assert.assertTrue(response.getLocations().stream().anyMatch(location -> location.getIsoCountryCode2Digit().equals(countryCode)));
  }

}
