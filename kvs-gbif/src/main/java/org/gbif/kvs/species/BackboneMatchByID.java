package org.gbif.kvs.species;

import org.apache.commons.lang3.StringUtils;
import org.gbif.api.vocabulary.OccurrenceIssue;
import org.gbif.nameparser.NameParserGBIF;
import org.gbif.nameparser.api.NameParser;
import org.gbif.nameparser.api.ParsedName;
import org.gbif.nameparser.api.UnparsableNameException;
import org.gbif.rest.client.species.ChecklistbankService;
import org.gbif.rest.client.species.NameUsageSearchResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Set;

import static org.gbif.api.model.Constants.NUB_DATASET_KEY;
import static org.gbif.api.vocabulary.OccurrenceIssue.*;

/**
 * A utility class to perform the logic of locating a backbone taxon from a variety of IDs.
 * This will first make use of scientificNameID, then taxonID and then taxonConceptID, to locate a concept in the
 * checklists configured for that ID prefix. If a backbone key is found, then it is returned, otherwise it moves
 * on to the next identifier. While processing it will apply flags to indicate if the IDs are ignored, and will
 * additionally flag if the scientificName appears different on the record to that in the checklist.
 */
public class BackboneMatchByID {
  private static final Logger LOG = LoggerFactory.getLogger(BackboneMatchByID.class);

  private static NameParser PARSER = new NameParserGBIF(2000, 0, 3);
  private ChecklistbankService checklistbankService;
  private Map<String, String> prefixMapping;
  private Map<String, String> prefixToChecklistKey;

  public BackboneMatchByID(ChecklistbankService checklistbankService, Map<String, String> prefixMapping, Map<String, String> prefixToChecklistKey) {
    if (prefixMapping != null) {
      LOG.debug("{} prefixes supported for name/taxa ID matching", prefixToChecklistKey.size());
    } else {
      LOG.debug("No prefixes supported for name/taxa ID matching");
    }
    this.checklistbankService = checklistbankService;
    this.prefixMapping = prefixMapping;
    this.prefixToChecklistKey = prefixToChecklistKey;
  }

  Integer lookupBackboneKey(Identification identification, Set<OccurrenceIssue> issues) {
    Integer backboneUsageKey;
    String scientificName = identification.getScientificName();
    String scientificNameID = identification.getScientificNameID();
    String taxonID = identification.getTaxonID();
    String taxonConceptID = identification.getTaxonConceptID();

    // start with scientificNameID
    LOG.debug("Attempting to locate a backbone taxon for scientificNameID[{}]", scientificNameID);
    backboneUsageKey = lookupBackboneKey(scientificNameID, scientificName, issues, TAXON_MATCH_SCIENTIFIC_NAME_ID_IGNORED, SCIENTIFIC_NAME_ID_NOT_FOUND);

    // use taxonID if we haven't succeeded yet
    if (backboneUsageKey == null) {
      LOG.debug("Attempting to locate a backbone taxon for taxonID[{}]", taxonID);
      backboneUsageKey = lookupBackboneKey(taxonID, scientificName, issues, TAXON_MATCH_TAXON_ID_IGNORED, TAXON_ID_NOT_FOUND);
    } else {
      LOG.debug("Backbone key[{}] found using scientificNameID[{}]. taxonID was therefore ignored", backboneUsageKey, scientificNameID);
      if (taxonID != null) issues.add(TAXON_MATCH_TAXON_ID_IGNORED);
    }

    // use taxonConceptID if we haven't succeeded yet
    if (backboneUsageKey == null) {
      LOG.debug("Attempting to locate a backbone taxon for taxonConceptID[{}]", taxonConceptID);
      backboneUsageKey = lookupBackboneKey(taxonConceptID, scientificName, issues, TAXON_MATCH_TAXON_CONCEPT_ID_IGNORED, TAXON_CONCEPT_ID_NOT_FOUND);
    } else {
      LOG.debug("Backbone key[{}] found using scientificNameID[{}] or taxonID[{}]. taxonConceptID was therefore ignored", backboneUsageKey, scientificNameID, taxonID);
      if (taxonConceptID != null) issues.add(TAXON_MATCH_TAXON_CONCEPT_ID_IGNORED);
    }

    if (backboneUsageKey == null) LOG.debug("Backbone key was not found using any of the name or taxa IDs");
    return backboneUsageKey;
  }

  Integer lookupBackboneKey(String id, String name, Set<OccurrenceIssue> issues, OccurrenceIssue ignoredFlag, OccurrenceIssue notFoundFlag) {
    Integer backboneUsageKey = null;
    if (id != null) {
      LOG.debug("Attempting to locate a backbone taxon for ID[{}]", id);

      String adjustedID = prefixReplaceId(id, prefixMapping).trim();
      String checklistKey = checklistKeyForID(adjustedID, prefixToChecklistKey);
      if (checklistKey != null && adjustedID.length()>0) {

        // Short circuit: if the checklist is already pointing to the backbone we don't need any further lookup
        if (checklistKey.equalsIgnoreCase(NUB_DATASET_KEY.toString())) {
          try {
            backboneUsageKey = Integer.parseInt(adjustedID);
          } catch (NumberFormatException e) {
            issues.add(notFoundFlag);
            LOG.debug("Adjusted ID[{}] is no backbone key integer", adjustedID);
          }

        } else {
          NameUsageSearchResponse response = checklistbankService.lookupNameUsage(checklistKey, adjustedID);

          if (response.getResults().isEmpty()) {
            issues.add(notFoundFlag);
            LOG.debug("No matches found when looking up [{}] against checklist[{}]", adjustedID, checklistKey);

          } else if (response.getResults().size()==1) {
            NameUsageSearchResponse.Result match = response.getResults().get(0);
            LOG.debug("Looking up [{}] against checklist[{}] matched to scientificName[{}], nameUsageID[{}], backboneID[{}]", adjustedID, checklistKey, match.getScientificName(), match.getKey(), match.getNubKey());
            if (match.getNubKey() != null) {
              compareAndFlagDifferingNames(id, name, issues, match);
              backboneUsageKey = match.getNubKey();

            } else {
              issues.add(ignoredFlag); // concept found, but ignored as it doesn't snap to the backbone
            }

          } else {
            issues.add(ignoredFlag); // we did not find a single concept to use (an edge case indicating bad checklist data)
            LOG.debug("{} matches found looking up [{}] against checklist[{}]. Ignored as ambiguous", response.getResults().size(), adjustedID, checklistKey);
          }
        }
      } else {
        issues.add(ignoredFlag);
        LOG.debug("Adjusted ID[{}] is not configured to match against any checklist by it's prefix", adjustedID);
      }
    }
    return backboneUsageKey;
  }

  /**
   * Add a flag only if the provided name and match response can be parsed and the canonical names found to differ.
   * This lenient in behaviour as it is only a hint to publishers so should not fail.
   */
  static void compareAndFlagDifferingNames(String id, String name, Set<OccurrenceIssue> issues, NameUsageSearchResponse.Result match) {
    try {
      ParsedName recordName = PARSER.parse(name, null, null);
      ParsedName foundName = PARSER.parse(match.getScientificName(), null, null);
      // flag as a hint if we have successfully parsed and found the canonical names to differ
      if (!StringUtils.equals(recordName.canonicalNameWithoutAuthorship(), foundName.canonicalNameWithoutAuthorship())) {
        LOG.debug("Canonical name[{}] found using ID[{}] and parsed from [{}] does not match canonical name[{}] on record, after parsing",
            foundName.canonicalNameWithoutAuthorship(), id, match.getScientificName(), recordName.canonicalNameWithoutAuthorship());
        issues.add(SCIENTIFIC_NAME_AND_ID_INCONSISTENT); // we will still use the found concept though
      }
    } catch(UnparsableNameException e) {
      LOG.warn("Unable to parse names for ID[{}], foundName[{}], recordName[{}]", id, match.getScientificName(), name, e);
    } catch(Exception e) {
      LOG.error("Unexpected error parsing and comparing canonical names for ID[{}], foundName[{}], recordName[{}]", id, match.getScientificName(), name, e);
    }
  }

  /** Apply any prefix replacements to the ID */
  static String prefixReplaceId(String id, Map<String, String> prefixMapping) {
    if (prefixMapping != null) {
      for (Map.Entry<String, String> e : prefixMapping.entrySet()) {
        if (id.startsWith(e.getKey())) {
          String orig = id;
          id = id.replaceFirst("\\Q" + e.getKey() + "\\E", e.getValue()); // verbatim replacement
          LOG.debug("ID adjusted due to prefix matching from [{}] to [{}]", orig, id);
        }
      }
    }
    return id;
  }

  /** Provide any configured datasetKey for the prefix of the ID */
  static String checklistKeyForID(String id, Map<String, String> datasetMapping) {
    if (datasetMapping != null) {
      for (Map.Entry<String, String> e : datasetMapping.entrySet()) {
        if (id.startsWith(e.getKey())) {
          LOG.debug("ID[{}] prefix[{}] configured to match dataset[{}]", id, e.getKey(), e.getValue());
          return e.getValue();
        }
      }
    }
    return null;
  }
}
