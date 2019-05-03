package org.gbif.kvs.species;

import com.google.common.base.Strings;
import org.gbif.api.model.checklistbank.ParsedName;
import org.gbif.api.vocabulary.Rank;
import org.gbif.common.parsers.RankParser;

import java.util.Optional;

/** Converter to create queries for the name match service. */
public class TaxonParsers {

  private static final RankParser RANK_PARSER = RankParser.getInstance();

  private TaxonParsers() {}

  /** @return a type status parser. */
  private static Optional<Rank> parserRank(SpeciesMatchRequest request) {
    Rank rank = null;
    if (!Strings.isNullOrEmpty(request.getRank())) {
       rank = RANK_PARSER.parse(request.getRank()).getPayload();
    }

    if (rank == null && !Strings.isNullOrEmpty(request.getVerbatimTaxonRank())) {
      rank = RANK_PARSER.parse(request.getVerbatimTaxonRank()).getPayload();
    }

    return Optional.ofNullable(rank);
  }

  private static Rank fromFields(SpeciesMatchRequest speciesMatchRequest) {
    if (speciesMatchRequest.getGenus() == null) {
      return null;
    }
    if (speciesMatchRequest.getSpecificEpithet() == null) {
      return Rank.GENUS;
    }
    return speciesMatchRequest.getInfraspecificEpithet() != null ? Rank.INFRASPECIFIC_NAME : Rank.SPECIES;
  }

  private static String fromScientificName(String scientificName, String authorship) {
    boolean containsAuthorship =
      scientificName != null
            && !Strings.isNullOrEmpty(authorship)
            && !scientificName.toLowerCase().contains(authorship.toLowerCase());

    return containsAuthorship ? scientificName + " " + authorship : scientificName;
  }

  /**
   * Handle case when the scientific name is null and only given as atomized fields: genus &
   * speciesEpitheton
   */
  private static String fromGenericName(SpeciesMatchRequest speciesMatchRequest, String authorship) {
    ParsedName pn = new ParsedName();
    pn.setGenusOrAbove(Strings.isNullOrEmpty(speciesMatchRequest.getGenericName()) ? speciesMatchRequest.getGenus() : speciesMatchRequest.getGenericName());
    pn.setSpecificEpithet(speciesMatchRequest.getSpecificEpithet());
    pn.setInfraSpecificEpithet(speciesMatchRequest.getInfraspecificEpithet());
    pn.setAuthorship(authorship);
    return pn.canonicalNameComplete();
  }


  public static Rank interpretRank(SpeciesMatchRequest speciesMatchRequest) {
    return parserRank(speciesMatchRequest)
            .orElseGet(() -> fromFields(speciesMatchRequest));
  }


  /** Assembles the most complete scientific name based on full and individual name parts. */
  public static String interpretScientificName(SpeciesMatchRequest speciesMatchRequest) {

    String authorship =
        Optional.ofNullable(speciesMatchRequest.getScientificNameAuthorship())
            .orElse(null);

    return Optional.ofNullable(speciesMatchRequest.getScientificName())
        .map(scientificName -> fromScientificName(scientificName, authorship))
        .orElseGet(() -> fromGenericName(speciesMatchRequest, authorship));
  }

}