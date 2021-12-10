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
   * Fuzzy matches scientific names against the GBIF Backbone Taxonomy with the optional classification provided.
   * If a classification is provided and strict is not set to true, the default matching will also try to match against
   * these if no direct match is found for the name parameter alone.
   * @param kingdom Optional kingdom classification accepting a canonical name
   * @param phylum Optional phylum classification accepting a canonical name
   * @param clazz Optional class classification accepting a canonical name
   * @param order Optional order classification accepting a canonical name
   * @param family Optional family classification accepting a canonical name
   * @param genus Optional genus classification accepting a canonical name
   * @param rank Filters by taxonomic rank as given in our {@link org.gbif.api.vocabulary.Rank} enum
   * @param name A scientific name which can be either a case insensitive filter for a canonical namestring
   * @param verbose if true it shows alternative matches which were considered but then rejected
   * @param strict 	if true it (fuzzy) matches only the given name, but never a taxon in the upper classification
   * @return a possible null name match
   */
  NameUsageMatch match(String kingdom,String phylum, String clazz, String order, String family, String genus,
                       String rank, String name, boolean verbose, boolean strict);

  /**
   * Gets the IUCN RedList Category of a nubKey.
   * @param nubKey GBIF backbone key.
   * @return a possible null map containing the IUCN RedList Category
   */
  IucnRedListCategory getIucnRedListCategory(Integer nubKey);

}
