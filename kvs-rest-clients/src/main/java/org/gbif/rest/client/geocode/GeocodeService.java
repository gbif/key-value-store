package org.gbif.rest.client.geocode;

import java.io.Closeable;
import java.util.List;

/**
 * GBIF Geocode Service client.
 * This class is used for creation of Sync and Async clients. It is not exposed outside this package.
 */
public interface GeocodeService extends Closeable {

  /**
   * Gets the list of proposed geo-locations of coordinate.
   * @param latitude decimal latitude
   * @param longitude decimal longitude
   * @return a list of proposed locations, an empty list if no proposals were found
   */
  List<Location> reverse(Double latitude, Double longitude);
}
