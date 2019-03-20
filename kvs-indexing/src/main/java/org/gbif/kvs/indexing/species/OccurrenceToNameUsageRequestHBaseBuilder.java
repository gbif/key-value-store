package org.gbif.kvs.indexing.species;

import org.gbif.dwc.terms.DwcTerm;
import org.gbif.dwc.terms.GbifTerm;
import org.gbif.dwc.terms.Term;
import org.gbif.kvs.species.SpeciesMatchRequest;

import java.util.Optional;
import java.util.function.Consumer;

import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.util.Bytes;

/** Utility to convert HBase occurrence records into LatLng objects. */
class OccurrenceToNameUsageRequestHBaseBuilder {

  // Occurrence column family
  private static final byte[] CF = Bytes.toBytes("o");

  // each UnknownTerm is prefixed differently
  private static final String VERBATIM_TERM_PREFIX = "v_";

  /** Private constructor of utility class. */
  private OccurrenceToNameUsageRequestHBaseBuilder() {
    // DO NOTHING
  }

  /**
   * Translates an HBase record/result into a SpeciesMatchRequest object.
   *
   * @param result HBase row/col
   * @return a SpeciesMatchRequest object
   */
  public static SpeciesMatchRequest toSpeciesMatchRequest(Result result) {

    SpeciesMatchRequest.Builder builder = SpeciesMatchRequest.builder();

    // Interpret common
    putIfExists(result, DwcTerm.kingdom, builder::withKingdom);
    putIfExists(result, DwcTerm.phylum, builder::withPhylum);
    putIfExists(result, DwcTerm.class_, builder::withClazz);
    putIfExists(result, DwcTerm.order, builder::withOrder);
    putIfExists(result, DwcTerm.family, builder::withFamily);
    putIfExists(result, DwcTerm.genus, builder::withGenus);

    putIfExists(result, DwcTerm.specificEpithet, builder::withScientificName);
    putIfExists(result, DwcTerm.infraspecificEpithet, builder::withInfraspecificEpithet);
    putIfExists(result, DwcTerm.genus, builder::withGenus);

    putIfExists(result, DwcTerm.taxonRank, builder::withRank);
    putIfExists(result, DwcTerm.verbatimTaxonRank, builder::withVerbatimRank);

    putIfExists(result, GbifTerm.genericName, builder::withGenericName);
    putIfExists(result, DwcTerm.scientificName, builder::withScientificName);
    putIfExists(result, DwcTerm.scientificNameAuthorship, builder::withScientificNameAuthorship);

    return builder.build();
  }

  /**
   * Reads the verbatim value associated to a term into the consumer 'with'.
   * @param result HBase result
   * @param term verbatim field/term
   * @param with consumer
   */
  private static void putIfExists(Result result, Term term, Consumer<String> with) {
    Optional.ofNullable(result.getValue(CF, Bytes.toBytes(VERBATIM_TERM_PREFIX + term.simpleName())))
        .map(Bytes::toString).ifPresent(with);
  }
}
