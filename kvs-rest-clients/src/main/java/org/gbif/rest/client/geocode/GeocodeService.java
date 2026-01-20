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

import feign.QueryMap;
import feign.RequestLine;
import org.gbif.kvs.geocode.GeocodeRequest;


/**
 * GBIF Geocode Service client.
 */
public interface GeocodeService {

  /**
   * Gets the list of proposed geolocations of coordinate.
   * @param latLng  the latitude and longitude
   * @return a list of proposed locations, an empty list if no proposals were found
   */
  @RequestLine("GET geocode/reverse")
  GeocodeResponse reverse(@QueryMap GeocodeRequest latLng);
}
