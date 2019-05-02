package org.gbif.rest.client.geocode;

import java.util.Collection;

/**
 * GBIF Geocode Service client.
 * This class is used for creation of Sync and Async clients. It is not exposed outside this package.
 */
public interface GeocodeService {

  /**
   * Gets the list of proposed geo-locations of coordinate.
   * @param latitude decimal latitude
   * @param longitude decimal longitude
   * @return a list of proposed locations, an empty collections if no proposals were found
   */
  Collection<Location> reverse(Double latitude, Double longitude);
}
