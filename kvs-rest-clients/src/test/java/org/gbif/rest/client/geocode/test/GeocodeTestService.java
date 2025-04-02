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
package org.gbif.rest.client.geocode.test;

import org.gbif.kvs.geocode.GeocodeRequest;
import org.gbif.rest.client.geocode.GeocodeResponse;
import org.gbif.rest.client.geocode.GeocodeService;

import java.util.Collections;

/**
 * Test service that uses a list of centroids of known countries.
 */
public class GeocodeTestService implements GeocodeService {

  private static final CountryCentroids COUNTRY_CENTROIDS = new CountryCentroids();

  /**
   * Performs the Geocode lookup using the tests centroids data.
   * If the coordinate is not found in the test data, return an empty list.
   *
   * @param latLng the latitude and longitude to reverse geocode
   * @return a List with a single Geocode response, and empty List if the coordinate do not resolve to a country
   */
  @Override
  public GeocodeResponse reverse(GeocodeRequest latLng) {
    return new GeocodeResponse(COUNTRY_CENTROIDS.findByCoordinate(latLng.getLat(), latLng.getLng()).map(country -> {
              GeocodeResponse.Location location = new GeocodeResponse.Location();
              location.setName(country.getName());
              location.setIsoCountryCode2Digit(country.getIsoCode());
              location.setType("Political");
              location.setSource("GBIF test data");
              return Collections.singletonList(location);
            }).orElse(Collections.emptyList()));
  }
}
