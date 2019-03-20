package org.gbif.kvs.species;

import org.gbif.common.parsers.utils.ClassificationUtils;
import org.gbif.dwc.terms.DwcTerm;
import org.gbif.dwc.terms.GbifTerm;
import org.gbif.kvs.hbase.Indexable;

import java.io.Serializable;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.StringJoiner;

import com.google.common.collect.ImmutableMap;

/**
 * Represents a request to the species name match service.
 * This class is used mostly to efficiently store it as a lookup mechanism for caching.
 */
public class SpeciesMatchRequest implements Serializable, Indexable {

  private final String kingdom;
  private final String phylum;
  private final String clazz;
  private final String order;
  private final String family;
  private final String genus;
  private final String specificEpithet;
  private final String infraspecificEpithet;
  private final String rank;
  private final String verbatimTaxonRank;
  private final String scientificName;
  private final String genericName;
  private final String scientificNameAuthorship;

  /**
   * Full constructor.
   */
  private SpeciesMatchRequest(String kingdom, String phylum, String clazz, String order, String family, String genus,
                              String specificEpithet, String infraspecificEpithet, String rank, String verbatimTaxonRank,
                              String scientificName, String genericName, String scientificNameAuthorship) {
    this.kingdom = kingdom;
    this.phylum = phylum;
    this.clazz = clazz;
    this.order = order;
    this.family = family;
    this.genus = genus;
    this.specificEpithet = specificEpithet;
    this.infraspecificEpithet = infraspecificEpithet;
    this.rank = rank;
    this.verbatimTaxonRank = verbatimTaxonRank;
    this.scientificName = scientificName;
    this.genericName = genericName;
    this.scientificNameAuthorship = scientificNameAuthorship;
  }

  public String getKingdom() {
    return kingdom;
  }

  public String getPhylum() {
    return phylum;
  }

  public String getClazz() {
    return clazz;
  }

  public String getOrder() {
    return order;
  }

  public String getFamily() {
    return family;
  }

  public String getGenus() {
    return genus;
  }

  public String getSpecificEpithet() {
    return specificEpithet;
  }

  public String getInfraspecificEpithet() {
    return infraspecificEpithet;
  }

  public String getRank() {
    return rank;
  }

  public String getVerbatimTaxonRank() {
    return verbatimTaxonRank;
  }

  public String getScientificName() {
    return scientificName;
  }

  public String getGenericName() {
    return genericName;
  }

  public String getScientificNameAuthorship() {
    return scientificNameAuthorship;
  }

  @Override
  public String getLogicalKey() {
    return appendIgnoreNulls(kingdom, phylum, clazz, order, family, genus, specificEpithet,
                            infraspecificEpithet, rank, verbatimTaxonRank, scientificName, genericName,
                            scientificNameAuthorship);
  }

  private String appendIgnoreNulls(String... values) {
    StringBuilder stringBuilder = new StringBuilder();
    for(String value : values) {
      Optional.ofNullable(value).map(String::trim).ifPresent(stringBuilder::append);
    }
    return stringBuilder.toString();
  }

  /**
   * Converts a {@link Map} of terms to {@link Map} with the params needed to call the {@link
   * SpeciesMatchv2Service}.
   */
  public Map<String, String> asTermMap() {

    ImmutableMap.Builder<String, String> map = ImmutableMap.builder();

    // Interpret common
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
    Optional.ofNullable(genericName).ifPresent(v -> map.put(GbifTerm.genericName.simpleName(), v));

    return map.build();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    SpeciesMatchRequest that = (SpeciesMatchRequest) o;
    return Objects.equals(kingdom, that.kingdom) &&
        Objects.equals(phylum, that.phylum) &&
        Objects.equals(clazz, that.clazz) &&
        Objects.equals(order, that.order) &&
        Objects.equals(family, that.family) &&
        Objects.equals(genus, that.genus) &&
        Objects.equals(specificEpithet, that.specificEpithet) &&
        Objects.equals(infraspecificEpithet, that.infraspecificEpithet) &&
        Objects.equals(rank, that.rank) &&
        Objects.equals(verbatimTaxonRank, that.verbatimTaxonRank) &&
        Objects.equals(scientificName, that.scientificName) &&
        Objects.equals(genericName, that.genericName) &&
        Objects.equals(scientificNameAuthorship, that.scientificNameAuthorship);
  }

  @Override
  public int hashCode() {
    return Objects.hash(kingdom, phylum, clazz, order, family, genus, specificEpithet, infraspecificEpithet,
                        rank, verbatimTaxonRank, scientificName, genericName, scientificNameAuthorship);
  }

  @Override
  public String toString() {
    return new StringJoiner(", ", SpeciesMatchRequest.class.getSimpleName() + "[", "]")
        .add("kingdom='" + kingdom + "'")
        .add("phylum='" + phylum + "'")
        .add("clazz='" + clazz + "'")
        .add("order='" + order + "'")
        .add("family='" + family + "'")
        .add("genus='" + genus + "'")
        .add("specificEpithet='" + specificEpithet + "'")
        .add("infraspecificEpithet='" + infraspecificEpithet + "'")
        .add("rank=" + rank)
        .add("verbatimTaxonRank=" + verbatimTaxonRank)
        .add("scientificName='" + scientificName + "'")
        .add("genericName='" + genericName + "'")
        .add("scientificNameAuthorship='" + scientificNameAuthorship + "'")
        .toString();
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
      this.specificEpithet = ClassificationUtils.clean(specificEpithet);
      return this;
    }

    public Builder withInfraspecificEpithet(String infraspecificEpithet) {
      this.infraspecificEpithet = ClassificationUtils.clean(infraspecificEpithet);
      return this;
    }

    public Builder withRank(String rank) {
      this.rank = ClassificationUtils.clean(rank);
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
      this.scientificNameAuthorship = ClassificationUtils.clean(scientificNameAuthorship);
      return this;
    }

    public SpeciesMatchRequest build() {
      return new SpeciesMatchRequest(kingdom, phylum, clazz, order, family, genus, specificEpithet,
                                     infraspecificEpithet, rank, verbatimRank, scientificName, genericName,
                                     scientificNameAuthorship);
    }
  }
}
