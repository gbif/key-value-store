package org.gbif.kvs.grscicoll;

import org.junit.Test;

import static org.junit.Assert.*;

public class GrscicollLookupRequestTest {

    @Test
    public void testEqualsAndHashCode_sameValues() {
        GrscicollLookupRequest req1 = new GrscicollLookupRequest(
                "INST1", "OWNER1", "INSTID1", "COLL1", "COLLID1", "DS1", "US");
        GrscicollLookupRequest req2 = new GrscicollLookupRequest(
                "INST1", "OWNER1", "INSTID1", "COLL1", "COLLID1", "DS1", "US");

        assertEquals("Objects with same field values should be equal", req1, req2);
        assertEquals("Hash codes should match for equal objects", req1.hashCode(), req2.hashCode());
    }

    @Test
    public void testEquals_differentField() {
        GrscicollLookupRequest req1 = new GrscicollLookupRequest();
        req1.setInstitutionCode("INST1");

        GrscicollLookupRequest req2 = new GrscicollLookupRequest();
        req2.setInstitutionCode("INST2");

        assertNotEquals("Different institutionCode should not be equal", req1, req2);
    }

    @Test
    public void testEquals_sameInstance() {
        GrscicollLookupRequest req = new GrscicollLookupRequest();
        assertEquals("An object should equal itself", req, req);
    }

    @Test
    public void testEquals_null() {
        GrscicollLookupRequest req = new GrscicollLookupRequest();
        assertNotEquals("An object should not equal null", req, null);
    }

    @Test
    public void testEquals_differentClass() {
        GrscicollLookupRequest req = new GrscicollLookupRequest();
        String other = "Not a request";
        assertNotEquals("An object should not equal a different type", req, other);
    }

    @Test
    public void testLogicalKey_withValues() {
        GrscicollLookupRequest req = new GrscicollLookupRequest(
                "INST1", "OWNER1", "INSTID1", "COLL1", "COLLID1", "DS1", "US");

        String key = req.getLogicalKey();

        assertTrue("Logical key should contain institutionCode", key.contains("INST1"));
        assertTrue("Logical key should contain collectionCode", key.contains("COLL1"));
        assertTrue("Logical key should contain datasetKey", key.contains("DS1"));
    }

    @Test
    public void testLogicalKey_withNulls() {
        GrscicollLookupRequest req = new GrscicollLookupRequest();
        String key = req.getLogicalKey();

        assertNotNull("Logical key should not be null when fields are null", key);
        assertTrue("Logical key should contain 'null' placeholders", key.contains("null"));
    }

    @Test
    public void testLogicalKey_withEmptyStrings() {
        GrscicollLookupRequest req = new GrscicollLookupRequest(
                "", "", "", "", "", "", "");

        String key = req.getLogicalKey();

        assertNotNull("Logical key should not be null with empty strings", key);
        assertTrue("Logical key should contain 'null' placeholders for empty values", key.contains("null"));
    }
}
