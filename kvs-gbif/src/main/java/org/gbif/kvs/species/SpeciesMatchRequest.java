package org.gbif.kvs.species;

import org.gbif.api.vocabulary.Rank;
import org.gbif.kvs.hbase.Indexable;

import java.io.Serializable;
import java.util.Objects;
import java.util.StringJoiner;

/**
 * Represents a request to the species name match service.
 * This class is used mostly to efficiently store it as a lookup mechanism for caching.
 */
public class SpeciesMatchRequest implements Serializable, Indexable {

  private final String kingdom;
  private final String phylum;
  private final String  class_;
  private final String  order;
  private final String family;
  private final String genus;
  private final Rank rank;
  private final String scientificName;

  /**
   * Full constructor.
   */
  private SpeciesMatchRequest(String kingdom, String phylum, String class_, String order, String family, String genus,
                              Rank rank, String scientificName) {
    this.kingdom = kingdom;
    this.phylum = phylum;
    this.class_ = class_;
    this.order = order;
    this.family = family;
    this.genus = genus;
    this.rank = rank;
    this.scientificName = scientificName;
  }

  public String getKingdom() {
    return kingdom;
  }

  public String getPhylum() {
    return phylum;
  }

  public String getClass_() {
    return class_;
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

  public Rank getRank() {
    return rank;
  }

  public String getScientificName() {
    return scientificName;
  }

  @Override
  public String getLogicalKey() {
    return this.kingdom + this.phylum + this.class_ + this.order + this.family + this.genus + this.rank + this.scientificName;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    SpeciesMatchRequest that = (SpeciesMatchRequest) o;
    return Objects.equals(kingdom, that.kingdom) &&
        Objects.equals(phylum, that.phylum) &&
        Objects.equals(class_, that.class_) &&
        Objects.equals(order, that.order) &&
        Objects.equals(family, that.family) &&
        Objects.equals(genus, that.genus) &&
        rank == that.rank &&
        Objects.equals(scientificName, that.scientificName);
  }

  @Override
  public int hashCode() {
    return Objects.hash(kingdom, phylum, class_, order, family, genus, rank, scientificName);
  }

  @Override
  public String toString() {
    return new StringJoiner(", ", SpeciesMatchRequest.class.getSimpleName() + "[", "]")
        .add("kingdom='" + kingdom + "'")
        .add("phylum='" + phylum + "'")
        .add("class_='" + class_ + "'")
        .add("order='" + order + "'")
        .add("family='" + family + "'")
        .add("genus='" + genus + "'")
        .add("rank=" + rank)
        .add("scientificName='" + scientificName + "'")
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
    private String class_;
    private String order;
    private String family;
    private String genus;
    private Rank rank;
    private String scientificName;

    public Builder withKingdom(String kingdom) {
      this.kingdom = kingdom;
      return this;
    }

    public Builder withPhylum(String phylum) {
      this.phylum = phylum;
      return this;
    }

    public Builder withClass_(String class_) {
      this.class_ = class_;
      return this;
    }

    public Builder withOrder(String order) {
      this.order = order;
      return this;
    }

    public Builder withFamily(String family) {
      this.family = family;
      return this;
    }

    public Builder withGenus(String genus) {
      this.genus = genus;
      return this;
    }

    public Builder withRank(Rank rank) {
      this.rank = rank;
      return this;
    }

    public Builder withScientificName(String scientificName) {
      this.scientificName = scientificName;
      return this;
    }

    public SpeciesMatchRequest build() {
      return new SpeciesMatchRequest(kingdom, phylum, class_, order, family, genus, rank, scientificName);
    }
  }
}
