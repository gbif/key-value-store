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
package org.gbif.kvs.indexing.species;

import org.gbif.dwc.terms.DwcTerm;
import org.gbif.dwc.terms.Term;
import org.gbif.kvs.species.NameUsageMatchRequest;

import java.util.Optional;
import java.util.function.Consumer;

import org.apache.avro.generic.GenericRecord;
import org.apache.beam.sdk.transforms.SerializableFunction;

/** Utility to convert HBase occurrence records into Identification objects. */
class AvroOccurrenceRecordToNameUsageRequest implements SerializableFunction<GenericRecord, NameUsageMatchRequest> {

  // each UnknownTerm is prefixed differently
  private static final String VERBATIM_TERM_PREFIX = "v_";

  /** Private constructor of utility class. */
  AvroOccurrenceRecordToNameUsageRequest() {
    // DO NOTHING
  }

  /**
   * Translates an HBase record/result into a Identification object.
   */
  @Override
  public NameUsageMatchRequest apply(GenericRecord input) {
    NameUsageMatchRequest.NameUsageMatchRequestBuilder builder = NameUsageMatchRequest.builder();

    // Interpret common
    putIfExists(input, DwcTerm.kingdom, builder::withKingdom);
    putIfExists(input, DwcTerm.phylum, builder::withPhylum);
    putIfExists(input, DwcTerm.class_, builder::withClazz);
    putIfExists(input, DwcTerm.order, builder::withOrder);
    putIfExists(input, DwcTerm.family, builder::withFamily);
    putIfExists(input, DwcTerm.genus, builder::withGenus);
    putIfExists(input, DwcTerm.scientificName, builder::withScientificName);
    putIfExists(input, DwcTerm.genericName, builder::withGenericName);
    putIfExists(input, DwcTerm.specificEpithet, builder::withScientificName);
    putIfExists(input, DwcTerm.infraspecificEpithet, builder::withInfraspecificEpithet);
    putIfExists(input, DwcTerm.scientificNameAuthorship, builder::withScientificNameAuthorship);
    putIfExists(input, DwcTerm.taxonRank, builder::withTaxonRank);
    putIfExists(input, DwcTerm.verbatimTaxonRank, builder::withVerbatimTaxonRank); // will end up ignored if rank exists

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
