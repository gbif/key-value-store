package org.gbif.kvs.species;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;
import org.gbif.api.vocabulary.OccurrenceIssue;
import org.gbif.rest.client.species.ChecklistbankService;
import org.gbif.rest.client.species.IucnRedListCategory;
import org.gbif.rest.client.species.NameUsageMatch;
import org.gbif.rest.client.species.NameUsageSearchResponse;
import org.junit.Test;

import java.io.IOException;
import java.util.Map;

import static org.junit.Assert.*;

import static org.gbif.kvs.species.BackboneMatchTest.MockChecklistbank.*;

/**
 * These test the behaviour of the backbone lookup behaviour including the expectations when using the IDs.
 */
public class BackboneMatchTest {

  MockChecklistbank clb = new MockChecklistbank();
  Map<String, String> prefixes = ImmutableMap.of("http://marinespecies.org/data.php?id=", "urn:lsid:marinespecies.org:taxname:");
  Map<String, String> prefixToDataset = ImmutableMap.of("urn:lsid:marinespecies.org:taxname:", wormsKey);
  BackboneMatchByID idMatcher = new BackboneMatchByID(clb, prefixes, prefixToDataset);

  public BackboneMatchTest() throws IOException {
    // Exception if JSON files are corrupt
  }

  @Test
  public void testNoIds() {
    Identification id = Identification.builder()
        .withScientificName("Salmo salar")
        .build();
    NameUsageMatch u1 = NameUsageMatchKVStoreFactory.matchAndDecorate(clb, id, idMatcher);
    assertEquals(backboneSalmonKey.intValue(), u1.getUsage().getKey());
    assertTrue(u1.getIssues().size() == 0);
  }

  @Test
  public void testNoMatch() {
    Identification id = Identification.builder()
        .withScientificName("Fubar")
        .build();
    NameUsageMatch u1 = NameUsageMatchKVStoreFactory.matchAndDecorate(clb, id, idMatcher);
    assertEquals(u1.getDiagnostics().getMatchType(), org.gbif.api.model.checklistbank.NameUsageMatch.MatchType.NONE);
    assertTrue(u1.getIssues().size() == 0);
  }

  @Test
  public void testScientificNameIDPreference() {
    Identification id = Identification.builder()
        .withScientificName("Salmo salar") // Not a Cod!
        .withScientificNameID(wormsCodKey) // the local ID
        .withTaxonID(wormsSalmonKey) // the local ID
        .withTaxonConceptID(wormsSalmonKey) // the local ID
        .build();
    NameUsageMatch u1 = NameUsageMatchKVStoreFactory.matchAndDecorate(clb, id, idMatcher);
    assertEquals(backboneCodKey.intValue(), u1.getUsage().getKey());
    assertEquals(4, u1.getIssues().size());
    assertTrue(u1.getIssues().contains(OccurrenceIssue.SCIENTIFIC_NAME_AND_ID_INCONSISTENT)); // name is not a cod
    assertTrue(u1.getIssues().contains(OccurrenceIssue.TAXON_MATCH_NAME_AND_ID_AMBIGUOUS)); // name search finds a cod
    assertTrue(u1.getIssues().contains(OccurrenceIssue.TAXON_MATCH_TAXON_ID_IGNORED));
    assertTrue(u1.getIssues().contains(OccurrenceIssue.TAXON_MATCH_TAXON_CONCEPT_ID_IGNORED));
  }

  @Test
  public void testTaxonIDPreference() {
    Identification id = Identification.builder()
        .withScientificName("Salmo salar") // Not a Cod!
        .withScientificNameID(wormsIDPrefix + "NONSENSE_ID")
        .withTaxonID(wormsCodKey) // the local ID
        .withTaxonConceptID(wormsSalmonKey) // the local ID
        .build();
    NameUsageMatch u1 = NameUsageMatchKVStoreFactory.matchAndDecorate(clb, id, idMatcher);
    assertEquals(backboneCodKey.intValue(), u1.getUsage().getKey());
    assertEquals(4, u1.getIssues().size());
    assertTrue(u1.getIssues().contains(OccurrenceIssue.SCIENTIFIC_NAME_AND_ID_INCONSISTENT)); // name is not a cod
    assertTrue(u1.getIssues().contains(OccurrenceIssue.TAXON_MATCH_NAME_AND_ID_AMBIGUOUS)); // name search finds a cod
    assertTrue(u1.getIssues().contains(OccurrenceIssue.SCIENTIFIC_NAME_ID_NOT_FOUND));
    assertTrue(u1.getIssues().contains(OccurrenceIssue.TAXON_MATCH_TAXON_CONCEPT_ID_IGNORED));
  }

  @Test
  public void testTaxonConceptIDPreference() {
    Identification id = Identification.builder()
        .withScientificName("Salmo salar") // Not a Cod!
        .withScientificNameID(wormsIDPrefix + "NONSENSE_ID")
        .withTaxonID("NO_PREFIX_NONSENSE_ID")
        .withTaxonConceptID(wormsCodKey)
        .build();
    NameUsageMatch u1 = NameUsageMatchKVStoreFactory.matchAndDecorate(clb, id, idMatcher);
    assertEquals(backboneCodKey.intValue(), u1.getUsage().getKey());
    assertEquals(4, u1.getIssues().size());
    assertTrue(u1.getIssues().contains(OccurrenceIssue.SCIENTIFIC_NAME_AND_ID_INCONSISTENT)); // name is not a cod
    assertTrue(u1.getIssues().contains(OccurrenceIssue.TAXON_MATCH_NAME_AND_ID_AMBIGUOUS)); // name search finds a cod
    assertTrue(u1.getIssues().contains(OccurrenceIssue.SCIENTIFIC_NAME_ID_NOT_FOUND));
    assertTrue(u1.getIssues().contains(OccurrenceIssue.TAXON_MATCH_TAXON_ID_IGNORED)); // prefix not mapped
  }

  @Test
  public void testPrefixMapping() {
    Identification id = Identification.builder()
        .withScientificName("Not used")
        .withScientificNameID("http://marinespecies.org/data.php?id=126436") // should be converted to the LSID
        .build();
    NameUsageMatch u1 = NameUsageMatchKVStoreFactory.matchAndDecorate(clb, id, idMatcher);
    assertNotNull("A converted ID should find the cod backbone usage", u1.getUsage());
    assertEquals(backboneCodKey.intValue(), u1.getUsage().getKey());
  }

  /**
   * This simulates checklistbank service behaviour for 2 taxa (Cod and Salmon) that exist in the backbone and the
   * WoRMs dataset. The services will work as follows:
   * <ol>
   *   <li>The IUCN category information is always empty</li>
   *   <li>A name match for "Gadus morhua" with no taxon/name ID will return the cod record </li>
   *   <li>A name match for "Salmo salar" with no taxon/name ID will return the salmon record</li>
   *   <li>A match with scientificNameID of 1 will return the Cod record; no other name IDs will work</li>
   *   <li>A match with taxonConceptID of 2 will return the Salmon record; no other name IDs will work</li>
   *   <li>A search within datasetKey for local id "urn:lsid:marinespecies.org:taxname:126436" will return the backbone key for cod</li>
   *   <li>A search within datasetKey "Fish" for local id "urn:lsid:marinespecies.org:taxname:127186" will return the backbone key for salmon</li>
   *   <li>Any other search using different dataset keys will find no matches</li>
   * </ol>
   */
  static class MockChecklistbank implements ChecklistbankService {
    private NameUsageMatch cod;
    private NameUsageMatch salmon;
    private NameUsageMatch none;
    private NameUsageSearchResponse searchCod;
    private NameUsageSearchResponse searchNone;
    private NameUsageSearchResponse searchSalmon;

    static String wormsKey = "2d59e5db-57ad-41ff-97d6-11f5fb264527";
    static String wormsCodKey = "urn:lsid:marinespecies.org:taxname:126436";
    static String wormsSalmonKey = "urn:lsid:marinespecies.org:taxname:127186";

    static String wormsIDPrefix = "urn:lsid:marinespecies.org:taxname:";
    static Integer backboneCodKey = 8084280;
    static Integer backboneSalmonKey = 7595433;

    MockChecklistbank() throws IOException {
      ObjectMapper mapper = new ObjectMapper();
      cod = mapper.readValue(MockChecklistbank.class.getResourceAsStream("/species/cod.json"), NameUsageMatch.class);
      salmon = mapper.readValue(MockChecklistbank.class.getResourceAsStream("/species/salmon.json"), NameUsageMatch.class);
      none = mapper.readValue(MockChecklistbank.class.getResourceAsStream("/species/none.json"), NameUsageMatch.class);
      searchCod = mapper.readValue(MockChecklistbank.class.getResourceAsStream("/species/searchCod.json"), NameUsageSearchResponse.class);
      searchSalmon = mapper.readValue(MockChecklistbank.class.getResourceAsStream("/species/searchSalmon.json"), NameUsageSearchResponse.class);
      searchNone = mapper.readValue(MockChecklistbank.class.getResourceAsStream("/species/searchNone.json"), NameUsageSearchResponse.class);
    }


    @Override
    public NameUsageMatch match(Integer usageKey, String kingdom, String phylum, String clazz, String order, String family, String genus, String scientificName, String genericName, String specificEpithet, String infraspecificEpithet, String scientificNameAuthorship, String rank, boolean verbose, boolean strict) {
      if (backboneCodKey.equals(usageKey)) return cod;
      if (backboneSalmonKey.equals(usageKey)) return salmon;
      if ("Gadus morhua".equals(scientificName)) return cod;
      if ("Salmo salar".equals(scientificName)) return salmon;
      return none;
    }

    @Override
    public IucnRedListCategory getIucnRedListCategory(Integer nubKey) {
      return new IucnRedListCategory();
    }

    @Override
    public NameUsageSearchResponse lookupNameUsage(String datasetKey, String sourceId) {
      if (wormsKey.equals(datasetKey)) {
        if (wormsCodKey.equals(sourceId)) return searchCod;
        if (wormsSalmonKey.equals(sourceId)) return searchSalmon;
      }
      return searchNone;
    }

    @Override
    public void close() {
    }
  }
}