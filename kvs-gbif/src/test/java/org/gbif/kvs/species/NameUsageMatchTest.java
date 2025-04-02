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
package org.gbif.kvs.species;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class NameUsageMatchTest {

  @Test
  public void testKey() {
    assertEquals(
            "Z|A|B|C|1|2|3|4|5|6|6a|6b|7|8|9|10|11|GENUS",
            (NameUsageMatchRequest.builder()
                    .withChecklistKey("Z")
                    .withScientificNameID("A")
                    .withTaxonConceptID("B")
                    .withTaxonID("C")
                    .withKingdom("1")
                    .withPhylum("2")
                    .withClazz("3")
                    .withOrder("4")
                    .withFamily("5")
                    .withGenus("6")
                    .withSubgenus("6a")
                    .withSpecies("6b")
                    .withScientificName("7")
                    .withGenericName("8")
                    .withSpecificEpithet("9")
                    .withInfraspecificEpithet("10")
                    .withAuthorship("11")
                    .withVerbatimRank("I will be ignored")
                    .withRank("GENUS")
                    .build()
                    .getLogicalKey())
    );
  }
}
