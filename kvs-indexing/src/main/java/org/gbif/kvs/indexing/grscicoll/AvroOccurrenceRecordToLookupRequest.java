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
package org.gbif.kvs.indexing.grscicoll;

import org.gbif.api.vocabulary.Country;
import org.gbif.dwc.terms.DwcTerm;
import org.gbif.dwc.terms.GbifTerm;
import org.gbif.dwc.terms.Term;
import org.gbif.kvs.grscicoll.GrscicollLookupRequest;

import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;

import org.apache.avro.generic.GenericRecord;
import org.apache.beam.sdk.transforms.SerializableFunction;

/** Utility to convert HBase occurrence records into SpeciesMatchRequest objects. */
class AvroOccurrenceRecordToLookupRequest
    implements SerializableFunction<GenericRecord, GrscicollLookupRequest> {

  // each UnknownTerm is prefixed differently
  private static final String VERBATIM_TERM_PREFIX = "v_";
  private static final UUID EBIRD_DATASET_KEY = UUID.fromString("4fa7b334-ce0d-4e88-aaae-2e0c138d049e");

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

    Optional<String> datasetKey = getValue(input, GbifTerm.datasetKey);
    if (datasetKey.isPresent()) {
      builder.withDatasetKey(datasetKey.get());

      if (datasetKey.get().equals(EBIRD_DATASET_KEY.toString())) {
        builder.withCountry(Country.UNITED_STATES.getIso2LetterCode());
      } else {
        getValue(input, GbifTerm.publishingCountry).ifPresent(builder::withCountry);
      }
    }

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

  private static Optional<String> getValue(GenericRecord input, Term term) {
    return Optional.ofNullable(input.get(term.simpleName().toLowerCase()))
        .map(Object::toString);
  }
}
