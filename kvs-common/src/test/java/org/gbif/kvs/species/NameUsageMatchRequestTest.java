package org.gbif.kvs.species;

import org.junit.Test;

import static org.junit.Assert.*;

public class NameUsageMatchRequestTest {

    @Test
    public void testEqualsAndHashCode_sameValues() {
        NameUsageMatchRequest req1 = NameUsageMatchRequest.builder()
                .withChecklistKey("chk1")
                .withUsageKey("u1")
                .withTaxonID("t1")
                .withTaxonConceptID("tc1")
                .withScientificNameID("snid1")
                .withScientificName("Homo sapiens")
                .withScientificNameAuthorship("Linnaeus")
                .withTaxonRank("species")
                .withVerbatimTaxonRank("sp")
                .withGenericName("Homo")
                .withSpecificEpithet("sapiens")
                .withKingdom("Animalia")
                .withPhylum("Chordata")
                .withClazz("Mammalia")
                .withOrder("Primates")
                .withSuperfamily("Hominoidea")
                .withFamily("Hominidae")
                .withGenus("Homo")
                .withSpecies("Homo sapiens")
                .withStrict(true)
                .withVerbose(false)
                .build();

        NameUsageMatchRequest req2 = NameUsageMatchRequest.builder()
                .withChecklistKey("chk1")
                .withUsageKey("u1")
                .withTaxonID("t1")
                .withTaxonConceptID("tc1")
                .withScientificNameID("snid1")
                .withScientificName("Homo sapiens")
                .withScientificNameAuthorship("Linnaeus")
                .withTaxonRank("species")
                .withVerbatimTaxonRank("sp")
                .withGenericName("Homo")
                .withSpecificEpithet("sapiens")
                .withKingdom("Animalia")
                .withPhylum("Chordata")
                .withClazz("Mammalia")
                .withOrder("Primates")
                .withSuperfamily("Hominoidea")
                .withFamily("Hominidae")
                .withGenus("Homo")
                .withSpecies("Homo sapiens")
                .withStrict(true)
                .withVerbose(false)
                .build();

        assertEquals("Objects with same field values should be equal", req1, req2);
        assertEquals("Hash codes should match for equal objects", req1.hashCode(), req2.hashCode());
    }

    @Test
    public void testEquals_differentField() {
        NameUsageMatchRequest req1 = NameUsageMatchRequest.builder()
                .withScientificName("Homo sapiens")
                .build();

        NameUsageMatchRequest req2 = NameUsageMatchRequest.builder()
                .withScientificName("Pan troglodytes")
                .build();

        assertNotEquals("Different scientificName should not be equal", req1, req2);
    }

    @Test
    public void testEquals_sameInstance() {
        NameUsageMatchRequest req = NameUsageMatchRequest.builder()
                .withScientificName("Homo sapiens")
                .build();

        assertEquals("An object should equal itself", req, req);
    }

    @Test
    public void testEquals_null() {
        NameUsageMatchRequest req = NameUsageMatchRequest.builder()
                .withScientificName("Homo sapiens")
                .build();

        assertNotEquals("An object should not equal null", req, null);
    }

    @Test
    public void testEquals_differentClass() {
        NameUsageMatchRequest req = NameUsageMatchRequest.builder()
                .withScientificName("Homo sapiens")
                .build();

        String notARequest = "Not a request";
        assertNotEquals("An object should not equal a different type", req, notARequest);
    }

    @Test
    public void testLogicalKey_withValues() {
        NameUsageMatchRequest req = NameUsageMatchRequest.builder()
                .withChecklistKey("chk1")
                .withScientificName("Homo sapiens")
                .withGenus("Homo")
                .withSpecies("Homo sapiens")
                .build();

        String key = req.getLogicalKey();

        assertTrue("Logical key should contain checklistKey", key.contains("chk1"));
        assertTrue("Logical key should contain genus", key.contains("Homo"));
        assertTrue("Logical key should contain species", key.contains("Homo sapiens"));
    }

    @Test
    public void testLogicalKey_withNulls() {
        NameUsageMatchRequest req = NameUsageMatchRequest.builder().build();

        String key = req.getLogicalKey();

        assertNotNull("Logical key should not be null even if all fields are null", key);
        assertTrue("Logical key should contain only separators when fields are null", key.contains("|"));
    }
}
