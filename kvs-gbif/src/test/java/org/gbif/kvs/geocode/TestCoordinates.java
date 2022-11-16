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

import java.util.Arrays;
import java.util.Collection;

/**
 * Holds common test coordinates for IT tests.
 */
public class TestCoordinates {

  static final Collection<Object[]> COORDINATES = Arrays.asList(new Object[][] {
      { LatLng.create(48.019573, 66.923684, null), "KZ" },
      { LatLng.create(35.937496, 14.375416, null), "MT" },
      { LatLng.create(-16.290154, -63.588653, null), "BO" },
      { LatLng.create(36.93, 13.37, null), null },
      { LatLng.create(-17.79125, 25.707917, 30_000.0), "ZM", "ZW" }
  });

  /**
   * Private constructor of utility class.
   */
  private TestCoordinates() {
    //NOTHING
  }

}
