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

import org.gbif.kvs.SaltedKeyGenerator;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collection;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

/**
 * Tests the number of buckets generated for a LatLng is in the expected range.
 */
@RunWith(Parameterized.class)
public class GeocodeRequestSaltedKeyTest {

  private static final int NUM_OF_BUCKETS = 2;
  private static final SaltedKeyGenerator SALTED_KEY_GENERATOR = new SaltedKeyGenerator(NUM_OF_BUCKETS,
                                                                                        StandardCharsets.UTF_8);

  private final GeocodeRequest indexableLatLng;

  /**
   * Creates an instance using a test latLng
   * @param indexableLatLng to be tested
   */
  public GeocodeRequestSaltedKeyTest(GeocodeRequest indexableLatLng) {
    this.indexableLatLng = indexableLatLng;
  }

  @Parameterized.Parameters(name = "{index}: IndexableLatLng({0})")
  public static Collection<Object[]> data() {
    return Arrays.asList(new Object[][]{
            {GeocodeRequest.create(48.019573, 66.923684, -50.0)},
            {GeocodeRequest.create(35.937496, 14.375416, 3000.0)},
            {GeocodeRequest.create(-16.290154, -63.588653, null)},
            {GeocodeRequest.create(36.93, 13.37, null)}
    });
  }

  /**
   * Is the generated salted key in the expected range.
   */
  @Test
  public void latLngSaltedKeyTest() {
    int bucket = Character.getNumericValue(new String(SALTED_KEY_GENERATOR.computeKey(indexableLatLng.getLogicalKey())).charAt(0));
    Assert.assertTrue("", bucket >= 0 && bucket < NUM_OF_BUCKETS);
  }
}
