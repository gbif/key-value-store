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
package org.gbif.rest.client.species;



import java.io.Closeable;

/**
 * GBIF Backbone name match and IUCN RedList services.
 */
public interface ChecklistbankService extends Closeable {

  /**
   * Fuzzy matches against the GBIF Backbone Taxonomy.
   * @return a possible null name match
   */
  NameUsageMatch match(Integer usageKey, String kingdom,String phylum, String clazz, String order, String family, String genus,
                       String scientificName, String genericName, String specificEpithet, String infraspecificEpithet,
                       String scientificNameAuthorship, String rank, boolean verbose, boolean strict);

  /**
   * Gets the IUCN RedList Category of a nubKey.
   * @param nubKey GBIF backbone key.
   * @return a possible null map containing the IUCN RedList Category
   */
  IucnRedListCategory getIucnRedListCategory(Integer nubKey);

  /**
   * Locates a nameUsage having the sourceId within the checklist.
   * @param datasetKey For the checklist to lookup
   * @param sourceId For the name usage within the checklist (e.g. a WorMS LSID)
   * @return the NameUsageSearchResponse containing the lookup result(s)
   */
  NameUsageSearchResponse lookupNameUsage(String datasetKey, String sourceId);
}
