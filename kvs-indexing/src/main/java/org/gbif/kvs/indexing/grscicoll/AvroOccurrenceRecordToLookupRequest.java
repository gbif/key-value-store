package org.gbif.kvs.indexing.grscicoll;

import java.util.Optional;
import java.util.function.Consumer;

import org.gbif.dwc.terms.DwcTerm;
import org.gbif.dwc.terms.GbifTerm;
import org.gbif.dwc.terms.Term;
import org.gbif.kvs.grscicoll.GrscicollLookupRequest;

import org.apache.avro.generic.GenericRecord;
import org.apache.beam.sdk.transforms.SerializableFunction;

/** Utility to convert HBase occurrence records into SpeciesMatchRequest objects. */
class AvroOccurrenceRecordToLookupRequest
    implements SerializableFunction<GenericRecord, GrscicollLookupRequest> {

  // each UnknownTerm is prefixed differently
  private static final String VERBATIM_TERM_PREFIX = "v_";

  /** Private constructor of utility class. */
  AvroOccurrenceRecordToLookupRequest() {
    // DO NOTHING
  }

  /** Translates an HBase record/result into a SpeciesMatchRequest object. */
  @Override
  public GrscicollLookupRequest apply(GenericRecord input) {
    GrscicollLookupRequest.Builder builder = GrscicollLookupRequest.builder();

    putVerbatimIfExists(input, DwcTerm.institutionCode, builder::withInstitutionCode);
    putVerbatimIfExists(input, DwcTerm.ownerInstitutionCode, builder::withOwnerInstitutionCode);
    putVerbatimIfExists(input, DwcTerm.institutionID, builder::withInstitutionId);
    putVerbatimIfExists(input, DwcTerm.collectionCode, builder::withCollectionCode);
    putVerbatimIfExists(input, DwcTerm.collectionID, builder::withCollectionId);
    putIfExists(input, GbifTerm.datasetKey, builder::withDatasetKey);

    // country is null for the moment. To fill it out we have to call the registry to take it
    // from the publishing organization of the dataset

    return builder.build();
  }

  /**
   * Reads the verbatim value associated to a term into the consumer 'with'.
   *
   * @param input Avro record
   * @param term verbatim field/term
   * @param with consumer
   */
  private static void putVerbatimIfExists(GenericRecord input, Term term, Consumer<String> with) {
    Optional.ofNullable(input.get(VERBATIM_TERM_PREFIX + term.simpleName().toLowerCase()))
        .map(Object::toString)
        .ifPresent(with);
  }

  private static void putIfExists(GenericRecord input, Term term, Consumer<String> with) {
    Optional.ofNullable(input.get(term.simpleName().toLowerCase()))
        .map(Object::toString)
        .ifPresent(with);
  }
}
