package org.gbif.kvs.species;

import org.gbif.api.vocabulary.Rank;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class SpeciesMatchRequestTest {

  @Test
  public void testKey() {
    assertEquals(
            "1|2|3|4|5|6|7|8|9|10|11|12",
            (SpeciesMatchRequest.builder()
                    .withKingdom("1")
                    .withPhylum("2")
                    .withClazz("3")
                    .withOrder("4")
                    .withFamily("5")
                    .withGenus("6")
                    .withScientificName("7")
                    .withGenericName("8")
                    .withSpecificEpithet("9")
                    .withInfraspecificEpithet("10")
                    .withScientificNameAuthorship("11")
                    .withVerbatimRank("I will be ignored")
                    .withRank("12").build().getLogicalKey()));

  }
}
