package org.gbif.rest.client.species;


import java.io.Closeable;
import java.util.Map;

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
  Map<String,String> getIucnRedListCategory(Integer nubKey);

}
