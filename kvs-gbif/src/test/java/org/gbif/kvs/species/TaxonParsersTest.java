package org.gbif.kvs.species;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class TaxonParsersTest {

  @Test
  public void authorshipTest() {
    assertEquals(
        "Acacia acinacea sensu Maslin (1996)",
        TaxonParsers.fromScientificName("Acacia acinacea s.l.", "sensu Maslin (1996)"));
    assertEquals(
        "Acacia acinacea s.l.", TaxonParsers.fromScientificName("Acacia acinacea s.l.", ""));
    assertEquals(
        "Acacia acinacea sensu Maslin (1996)",
        TaxonParsers.fromScientificName("Acacia acinacea sensu lato", "sensu Maslin (1996)"));
    assertEquals(
        "Acacia acinacea sensu Maslin (1996)",
        TaxonParsers.fromScientificName("Acacia acinacea s. lat.", "sensu Maslin (1996)"));

    // doesn't match the last group so the authorship in the name isn't removed
    assertEquals(
        "Acacia acinacea s.z. sensu Maslin (1996)",
        TaxonParsers.fromScientificName("Acacia acinacea s.z.", "sensu Maslin (1996)"));
  }
}
