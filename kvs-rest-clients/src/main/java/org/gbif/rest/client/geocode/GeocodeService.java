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

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.Closeable;
import java.util.List;

/**
 * GBIF Geocode Service client.
 */
@FeignClient(name = "geocode", url = "${geocode.baseApiUrl}")
public interface GeocodeService {

  /**
   * Gets the list of proposed geo-locations of coordinate.
   * @param latitude decimal latitude
   * @param longitude decimal longitude
   * @param uncertaintyMeters coordinate uncertainty in meters
   * @return a list of proposed locations, an empty list if no proposals were found
   */
  @RequestMapping(method = RequestMethod.GET, value = "geocode/reverse")
  GeocodeResponse reverse(
          @RequestParam("lat") Double latitude,
          @RequestParam("lng") Double longitude,
          @RequestParam("uncertaintyMeters") Double uncertaintyMeters);
}
