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

import org.apache.commons.lang3.StringUtils;
import org.gbif.common.parsers.utils.ClassificationUtils;
import org.gbif.kvs.hbase.Indexable;

import java.io.Serializable;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.avro.reflect.Nullable;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * This encapsulates the identification fields to lookup against the backbone taxonomy.
 * It includes verbatim fields for the names, the rank and the common name/taxa ID fields which will typically be found on Occurrence
 * records.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Identification implements Serializable, Indexable {

  @Nullable private String scientificNameID;
  @Nullable private String taxonConceptID;
  @Nullable private String taxonID;
  @Nullable private String kingdom;
  @Nullable private String phylum;
  @Nullable private String clazz;
  @Nullable private String order;
  @Nullable private String family;
  @Nullable private String genus;
  @Nullable private String scientificName;
  @Nullable private String genericName;
  @Nullable private String specificEpithet;
  @Nullable private String infraspecificEpithet;
  @Nullable private String scientificNameAuthorship;
  @Nullable private String rank;

  @Override
  /**
   * Returns a unique key for the request that strictly respects the fields populated in order.
   */
  public String getLogicalKey() {
    return Stream.of(scientificNameID, taxonConceptID, taxonID, kingdom, phylum, clazz, order, family, genus, scientificName,
                            genericName, specificEpithet, infraspecificEpithet, scientificNameAuthorship, rank)
            .map(s -> s == null ? "" : s.trim()).collect(Collectors.joining("|"));
  }

  /**
   * Creates a {@link Builder} instance.
   */
  public static Builder builder() {
    return new Builder();
  }

  public static class Builder {

    private String scientificNameID;
    private String taxonConceptID;
    private String taxonID;
    private String kingdom;
    private String phylum;
    private String clazz;
    private String order;
    private String family;
    private String genus;
    private String scientificName;
    private String genericName;
    private String specificEpithet;
    private String infraspecificEpithet;
    private String scientificNameAuthorship;
    private String rank;
    private String verbatimRank;

    // GBIF specific service requiring integers
    public Builder withScientificNameID(String scientificNameID) {
      this.scientificNameID = scientificNameID;
      return this;
    }
    public Builder withTaxonConceptID(String taxonConceptID) {
      this.taxonConceptID = taxonConceptID;
      return this;
    }
    public Builder withTaxonID(String taxonID) {
      this.taxonID = taxonID;
      return this;
    }
    public Builder withKingdom(String kingdom) {
      this.kingdom = ClassificationUtils.clean(kingdom);
      return this;
    }

    public Builder withPhylum(String phylum) {
      this.phylum = ClassificationUtils.clean(phylum);
      return this;
    }

    public Builder withClazz(String clazz) {
      this.clazz = ClassificationUtils.clean(clazz);
      return this;
    }

    public Builder withOrder(String order) {
      this.order = ClassificationUtils.clean(order);
      return this;
    }

    public Builder withFamily(String family) {
      this.family = ClassificationUtils.clean(family);
      return this;
    }

    public Builder withGenus(String genus) {
      this.genus = ClassificationUtils.clean(genus);
      return this;
    }

    public Builder withSpecificEpithet(String specificEpithet) {
      this.specificEpithet = ClassificationUtils.clean(specificEpithet);
      return this;
    }

    public Builder withInfraspecificEpithet(String infraspecificEpithet) {
      this.infraspecificEpithet = ClassificationUtils.clean(infraspecificEpithet);
      return this;
    }

    public Builder withRank(String rank) {
      this.rank = StringUtils.trimToNull(rank);
      return this;
    }

    /**
     * Will be ignored if a Rank is also provided.
     */
    public Builder withVerbatimRank(String verbatimRank) {
      this.verbatimRank = StringUtils.trimToNull(verbatimRank);
      return this;
    }

    public Builder withScientificName(String scientificName) {
      this.scientificName = ClassificationUtils.clean(scientificName);
      return this;
    }

    public Builder withGenericName(String genericName) {
      this.genericName = ClassificationUtils.clean(genericName);
      return this;
    }

    public Builder withScientificNameAuthorship(String scientificNameAuthorship) {
      this.scientificNameAuthorship = ClassificationUtils.cleanAuthor(scientificNameAuthorship);
      return this;
    }

    public Identification build() {
      // prefer the rank over verbatim rank
      String r = rank == null ? verbatimRank : rank;
      return new Identification(scientificNameID, taxonConceptID, taxonID, kingdom, phylum, clazz, order, family, genus, scientificName, genericName,
              specificEpithet, infraspecificEpithet, scientificNameAuthorship, r);
    }
  }
}
