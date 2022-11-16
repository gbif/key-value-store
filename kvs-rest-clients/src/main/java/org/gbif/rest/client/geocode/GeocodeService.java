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
   * @param uncertaintyMeters coordinate uncertainty in meters
   * @return a list of proposed locations, an empty list if no proposals were found
   */
  List<Location> reverse(Double latitude, Double longitude, Double uncertaintyMeters);

  /**
   * Gets the list of proposed geo-locations of coordinate.
   * @param latitude decimal latitude
   * @param longitude decimal longitude
   * @return a list of proposed locations, an empty list if no proposals were found
   */
  default List<Location> reverse(Double latitude, Double longitude) {
    return reverse(latitude, longitude, null);
  }
}
