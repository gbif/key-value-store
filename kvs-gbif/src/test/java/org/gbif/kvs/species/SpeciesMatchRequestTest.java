package org.gbif.kvs.species;

import org.gbif.api.vocabulary.Rank;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class SpeciesMatchRequestTest {

  @Test
  public void testKey() {
    assertEquals(
            "Animalia||||||||species||Puma concolor||Tim Robertson 2023",
            (SpeciesMatchRequest.builder()
                    .withKingdom("Animalia")
                    .withScientificName("Puma concolor")
                    .withScientificNameAuthorship("Tim Robertson 2023")
                    .withRank("species").build().getLogicalKey()));

  }
}
