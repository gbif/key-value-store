package org.gbif.kvs.geocode;

import java.util.Arrays;
import java.util.Collection;

/**
 * Holds common test coordinates for IT tests.
 */
public class TestCoordinates {

  static final Collection<Object[]> COORDINATES = Arrays.asList(new Object[][] {
      { LatLng.create(48.019573, 66.923684), "KZ" },
      { LatLng.create(35.937496, 14.375416), "MT" },
      { LatLng.create(-16.290154, -63.588653), "BO" },
      { LatLng.create(36.93, 13.37), null }
  });

  /**
   * Private constructor of utility class.
   */
  private TestCoordinates() {
    //NOTHING
  }

}
