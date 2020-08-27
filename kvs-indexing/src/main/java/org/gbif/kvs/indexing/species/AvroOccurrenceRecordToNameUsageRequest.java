package org.gbif.kvs.indexing.species;

import org.apache.avro.generic.GenericRecord;
import org.apache.beam.sdk.transforms.SerializableFunction;
import org.gbif.dwc.terms.DwcTerm;
import org.gbif.dwc.terms.GbifTerm;
import org.gbif.dwc.terms.Term;
import org.gbif.kvs.species.SpeciesMatchRequest;

import java.util.Optional;
import java.util.function.Consumer;

/** Utility to convert HBase occurrence records into SpeciesMatchRequest objects. */
class AvroOccurrenceRecordToNameUsageRequest implements SerializableFunction<GenericRecord, SpeciesMatchRequest> {

  // each UnknownTerm is prefixed differently
  private static final String VERBATIM_TERM_PREFIX = "v_";

  /** Private constructor of utility class. */
  AvroOccurrenceRecordToNameUsageRequest() {
    // DO NOTHING
  }

  /**
   * Translates an HBase record/result into a SpeciesMatchRequest object.
   */
  @Override
  public SpeciesMatchRequest apply(GenericRecord input) {
    SpeciesMatchRequest.Builder builder = SpeciesMatchRequest.builder();

    // Interpret common
    putIfExists(input, DwcTerm.kingdom, builder::withKingdom);
    putIfExists(input, DwcTerm.phylum, builder::withPhylum);
    putIfExists(input, DwcTerm.class_, builder::withClazz);
    putIfExists(input, DwcTerm.order, builder::withOrder);
    putIfExists(input, DwcTerm.family, builder::withFamily);
    putIfExists(input, DwcTerm.genus, builder::withGenus);

    putIfExists(input, DwcTerm.specificEpithet, builder::withScientificName);
    putIfExists(input, DwcTerm.infraspecificEpithet, builder::withInfraspecificEpithet);
    putIfExists(input, DwcTerm.genus, builder::withGenus);

    putIfExists(input, DwcTerm.taxonRank, builder::withRank);
    putIfExists(input, DwcTerm.verbatimTaxonRank, builder::withVerbatimRank);

    putIfExists(input, GbifTerm.genericName, builder::withGenericName);
    putIfExists(input, DwcTerm.scientificName, builder::withScientificName);
    putIfExists(input, DwcTerm.scientificNameAuthorship, builder::withScientificNameAuthorship);

    return builder.build();
  }

  /**
   * Reads the verbatim value associated to a term into the consumer 'with'.
   * @param input Avro record
   * @param term verbatim field/term
   * @param with consumer
   */
  private static void putIfExists(GenericRecord input, Term term, Consumer<String> with) {
    Optional.ofNullable(input.get(VERBATIM_TERM_PREFIX + term.simpleName().toLowerCase()))
        .map(Object::toString).ifPresent(with);
  }
}
