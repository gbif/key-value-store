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
package org.gbif.kvs.geocode;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.experimental.SuperBuilder;
import org.gbif.kvs.Keyed;

import java.io.Serializable;
import java.util.Objects;

/** Geographic Coordinate: latitude and longitude. */
@Data
@SuperBuilder(setterPrefix = "with")
@AllArgsConstructor
public class GeocodeRequest implements Keyed, Serializable {

  /**
   * Default value to use when uncertainty is not provided to avoid serialisation issues.
   * This dummy value is the circumference of the earth in meters as a negative number.
   */
  public static final Double EMPTY_UNCERTAINTY = -40075000d;

  protected Double latitude;
  protected Double longitude;
  protected Double uncertaintyMeters;

  /**
   * Factory method.
   * @param latitude decimal latitude
   * @param longitude decimal longitude
   * @return a new instance of LatLng
   */
  public static GeocodeRequest create(Double latitude, Double longitude) {
    return new GeocodeRequest(latitude, longitude, null);
  }

  /**
   * Factory method.
   * @param latitude decimal latitude
   * @param longitude decimal longitude
   * @param uncertaintyMeters uncertainty in metres
   * @return a new instance of LatLng
   */
  public static GeocodeRequest create(Double latitude, Double longitude, Double uncertaintyMeters) {
    return new GeocodeRequest(latitude, longitude, uncertaintyMeters);
  }

  /**
   * Is this coordinates valid?. Both can't be null and -90 <= latitude <= 90 and -180 <= longitude
   * <= 180.
   *
   * @return true if the coordinate is valid, false otherwise
   */
  public boolean isValid() {
    return Objects.nonNull(latitude)
        && Objects.nonNull(longitude)
        && latitude <= 90.0
        && latitude >= -90
        && longitude <= 180
        && longitude >= -180;
  }

  /**
   * Concatenates as a string the latitude and longitude.
   *
   * @return latitude + longitude
   */
  @Override
  public String getLogicalKey() {
    if (uncertaintyMeters == null || Objects.equals(EMPTY_UNCERTAINTY, uncertaintyMeters)) {
      return latitude.toString() + '|' + longitude.toString();
    } else {
      return latitude.toString() + '|' + longitude.toString() + '|' + uncertaintyMeters.toString();
    }
  }
}