package org.gbif.rest.client.geocode.test;

import org.gbif.rest.client.geocode.GeocodeResponse;
import org.gbif.rest.client.geocode.GeocodeService;

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
  public Collection<GeocodeResponse> reverse(Double latitude, Double longitude) {
    return COUNTRY_CENTROIDS.findByCoordinate(latitude, longitude).map(country -> {
              GeocodeResponse geocodeResponse = new GeocodeResponse();
              geocodeResponse.setCountryName(country.getName());
              geocodeResponse.setIsoCountryCode2Digit(country.getIsoCode());
              geocodeResponse.setType("Political");
              geocodeResponse.setSource("GBIF test data");
              return Collections.singletonList(geocodeResponse);
            }).orElse(Collections.emptyList());
  }
}
