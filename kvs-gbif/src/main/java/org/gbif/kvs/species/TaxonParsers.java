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

import java.util.Optional;
import java.util.regex.Pattern;

import org.gbif.api.model.checklistbank.ParsedName;
import org.gbif.api.vocabulary.Rank;
import org.gbif.common.parsers.RankParser;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Strings;

/** Converter to create queries for the name match service. */
public class TaxonParsers {

  private static final Pattern AUTHORSHIPS_PATTERN =
      Pattern.compile("(s\\.|sensu)(\\s*)(l\\.|lat[.o]|s\\.|str\\.|stricto)$");
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

  @VisibleForTesting
  static String fromScientificName(String scientificName, String authorship) {
    boolean containsAuthorship =
      scientificName != null
            && !Strings.isNullOrEmpty(authorship)
            && !scientificName.toLowerCase().contains(authorship.toLowerCase());

    return containsAuthorship
        ? AUTHORSHIPS_PATTERN.matcher(scientificName).replaceAll("").trim() + " " + authorship
        : scientificName;
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


  /**
   * Extract any stated rank from the request, but does not attempt to interpret it from the
   * fields.
   */
  public static Rank interpretRank(SpeciesMatchRequest speciesMatchRequest) {
    return parserRank(speciesMatchRequest).orElse(null);
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