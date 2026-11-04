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

import org.junit.Test;

import static org.junit.Assert.*;

public class GeocodeRequestTest {

    @Test
    public void testEqualsAndHashCode_sameValues2() {
        GeocodeRequest req1 = new GeocodeRequest(10.0, 20.0, null);
        GeocodeRequest req2 = new GeocodeRequest(10.0, 20.0, null);

        assertEquals("Objects with the same field values should be equal", req1, req2);
        assertEquals("Hash codes should match for equal objects", req1.hashCode(), req2.hashCode());
    }

    @Test
    public void testEqualsAndHashCode_sameValues() {
        GeocodeRequest req1 = new GeocodeRequest(10.0, 20.0, 5.0);
        GeocodeRequest req2 = new GeocodeRequest(10.0, 20.0, 5.0);

        assertEquals("Objects with the same field values should be equal", req1, req2);
        assertEquals("Hash codes should match for equal objects", req1.hashCode(), req2.hashCode());
    }

    @Test
    public void testEquals_differentLat() {
        GeocodeRequest req1 = new GeocodeRequest(10.0, 20.0, 5.0);
        GeocodeRequest req2 = new GeocodeRequest(11.0, 20.0, 5.0);

        assertNotEquals("Different latitude should not be equal", req1, req2);
    }

    @Test
    public void testEquals_differentLng() {
        GeocodeRequest req1 = new GeocodeRequest(10.0, 20.0, 5.0);
        GeocodeRequest req2 = new GeocodeRequest(10.0, 21.0, 5.0);

        assertNotEquals("Different longitude should not be equal", req1, req2);
    }

    @Test
    public void testEquals_differentUncertainty() {
        GeocodeRequest req1 = new GeocodeRequest(10.0, 20.0, 5.0);
        GeocodeRequest req2 = new GeocodeRequest(10.0, 20.0, 10.0);

        assertNotEquals("Different uncertainty should not be equal", req1, req2);
    }

    @Test
    public void testEquals_nullVsNonNull() {
        GeocodeRequest req1 = new GeocodeRequest(10.0, 20.0, null);
        GeocodeRequest req2 = new GeocodeRequest(10.0, 20.0, 5.0);

        assertNotEquals("Null vs non-null uncertainty should not be equal", req1, req2);
    }

    @Test
    public void testEquals_sameInstance() {
        GeocodeRequest req = new GeocodeRequest(10.0, 20.0, 5.0);

        assertEquals("An object should be equal to itself", req, req);
    }

    @Test
    public void testEquals_null() {
        GeocodeRequest req = new GeocodeRequest(10.0, 20.0, 5.0);

        assertNotEquals("An object should not be equal to null", req, null);
    }

    @Test
    public void testEquals_differentClass() {
        GeocodeRequest req = new GeocodeRequest(10.0, 20.0, 5.0);
        String other = "Not a GeocodeRequest";

        assertNotEquals("An object should not be equal to a different type", req, other);
    }
}
