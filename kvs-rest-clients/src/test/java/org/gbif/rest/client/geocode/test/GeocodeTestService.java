package org.gbif.rest.client.geocode.test;

import org.gbif.rest.client.geocode.GeocodeService;
import org.gbif.rest.client.geocode.Location;

import java.util.Collection;
import java.util.Collections;

/**
 * Test service that uses a list of centroids of known countries.
 */
public class GeocodeTestService implements GeocodeService {

  private static final CountryCentroids COUNTRY_CENTROIDS = new CountryCentroids();

  /**
   * Performs the Geocode lookup using the tests centroids data.
   * If the coordinate is not found in the test data, return an empty list.
   * @param latitude decimal latitude
   * @param longitude decimal longitude
   * @return a List with a single Geocode response, and empty List if the coordinate do not resolve to a country
   */
  @Override
  public Collection<Location> reverse(Double latitude, Double longitude) {
    return COUNTRY_CENTROIDS.findByCoordinate(latitude, longitude).map(country -> {
              Location location = new Location();
                location.setName(country.getName());
                location.setIsoCountryCode2Digit(country.getIsoCode());
                location.setType("Political");
                location.setSource("GBIF test data");
              return Collections.singletonList(location);
            }).orElse(Collections.emptyList());
  }

  @Override
  public void close() {
      //DO NOTHING
  }
}
