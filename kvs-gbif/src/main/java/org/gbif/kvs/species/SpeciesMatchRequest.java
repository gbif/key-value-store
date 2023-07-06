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

import org.gbif.common.parsers.utils.ClassificationUtils;
import org.gbif.dwc.terms.DwcTerm;
import org.gbif.kvs.hbase.Indexable;

import java.io.Serializable;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.avro.reflect.Nullable;

import com.google.common.collect.ImmutableMap;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Represents a request to the species name match service.
 * This class is used mostly to efficiently store it as a lookup mechanism for caching.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class SpeciesMatchRequest implements Serializable, Indexable {

  @Nullable private String kingdom;
  @Nullable private String phylum;
  @Nullable private String clazz;
  @Nullable private String order;
  @Nullable private String family;
  @Nullable private String genus;
  @Nullable private String specificEpithet;
  @Nullable private String infraspecificEpithet;
  @Nullable private String rank;
  @Nullable private String verbatimTaxonRank;
  @Nullable private String scientificName;
  @Nullable private String genericName;
  @Nullable private String scientificNameAuthorship;

  @Override
  /**
   * Returns a unique key for the request that strictly respects the fields populated.
   */
  public String getLogicalKey() {
    return Stream.of(kingdom, phylum, clazz, order, family, genus, specificEpithet,
                            infraspecificEpithet, rank, verbatimTaxonRank, scientificName, genericName,
                            scientificNameAuthorship)
            .map(s -> s == null ? "" : s.trim()).collect(Collectors.joining("|"));
  }

  /**
   * Converts a {@link Map} of terms to {@link Map} with the params needed to call the {@link
   * SpeciesMatchv2Service}.
   */
  public Map<String, String> asTermMap() {

    ImmutableMap.Builder<String, String> map = ImmutableMap.builder();

    Optional.ofNullable(kingdom).ifPresent(v -> map.put(DwcTerm.kingdom.simpleName(), v));
    Optional.ofNullable(phylum).ifPresent(v -> map.put(DwcTerm.phylum.simpleName(), v));
    Optional.ofNullable(clazz).ifPresent(v -> map.put(DwcTerm.class_.simpleName(), v));
    Optional.ofNullable(order).ifPresent(v -> map.put(DwcTerm.order.simpleName(), v));
    Optional.ofNullable(family).ifPresent(v -> map.put(DwcTerm.family.simpleName(), v));
    Optional.ofNullable(genus).ifPresent(v -> map.put(DwcTerm.genus.simpleName(), v));

    Optional.ofNullable(rank).ifPresent(v -> map.put(DwcTerm.taxonRank.simpleName(), v));
    Optional.ofNullable(verbatimTaxonRank).ifPresent(v -> map.put(DwcTerm.verbatimTaxonRank.simpleName(), v));


    Optional.ofNullable(scientificName).ifPresent(v -> map.put(DwcTerm.scientificName.simpleName(), v));
    Optional.ofNullable(scientificNameAuthorship).ifPresent(v -> map.put(DwcTerm.scientificNameAuthorship.simpleName(), v));
    Optional.ofNullable(genericName).ifPresent(v -> map.put(DwcTerm.genericName.simpleName(), v));

    return map.build();
  }

  /**
   * Creates a {@link Builder} instance.
   */
  public static Builder builder() {
    return new Builder();
  }

  public static class Builder {
    private String kingdom;
    private String phylum;
    private String clazz;
    private String order;
    private String family;
    private String genus;
    private String specificEpithet;
    private String infraspecificEpithet;
    private String rank;
    private String verbatimRank;
    private String scientificName;
    private String genericName;
    private String scientificNameAuthorship;

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
      this.specificEpithet = ClassificationUtils.cleanAuthor(specificEpithet);
      return this;
    }

    public Builder withInfraspecificEpithet(String infraspecificEpithet) {
      this.infraspecificEpithet = ClassificationUtils.cleanAuthor(infraspecificEpithet);
      return this;
    }

    public Builder withRank(String rank) {
      this.rank = rank;
      return this;
    }

    public Builder withVerbatimRank(String verbatimRank) {
      this.verbatimRank = ClassificationUtils.clean(verbatimRank);
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

    public SpeciesMatchRequest build() {
      return new SpeciesMatchRequest(kingdom, phylum, clazz, order, family, genus, specificEpithet,
                                     infraspecificEpithet, rank, verbatimRank, scientificName, genericName,
                                     scientificNameAuthorship);
    }
  }
}
