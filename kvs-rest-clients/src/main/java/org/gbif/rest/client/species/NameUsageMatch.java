package org.gbif.rest.client.species;

import org.gbif.api.v2.RankedName;
import org.gbif.api.vocabulary.TaxonomicStatus;
import org.gbif.api.model.checklistbank.NameUsageMatch.MatchType;

import java.io.Serializable;
import java.util.List;
import java.util.Objects;

import com.google.common.collect.Lists;

public class NameUsageMatch implements Serializable {

  private boolean synonym;
  private RankedName usage;
  private RankedName acceptedUsage;
  private NameUsageMatch.Nomenclature nomenclature;
  private List<RankedName> classification = Lists.newArrayList();
  private NameUsageMatch.Diagnostics diagnostics = new NameUsageMatch.Diagnostics();

  public boolean isSynonym() {
    return this.synonym;
  }

  public void setSynonym(boolean synonym) {
    this.synonym = synonym;
  }

  public RankedName getUsage() {
    return this.usage;
  }

  public void setUsage(RankedName usage) {
    this.usage = usage;
  }

  public RankedName getAcceptedUsage() {
    return this.acceptedUsage;
  }

  public void setAcceptedUsage(RankedName acceptedUsage) {
    this.acceptedUsage = acceptedUsage;
  }

  public NameUsageMatch.Nomenclature getNomenclature() {
    return this.nomenclature;
  }

  public void setNomenclature(NameUsageMatch.Nomenclature nomenclature) {
    this.nomenclature = nomenclature;
  }

  public List<RankedName> getClassification() {
    return this.classification;
  }

  public void setClassification(List<RankedName> classification) {
    this.classification = classification;
  }

  public NameUsageMatch.Diagnostics getDiagnostics() {
    return this.diagnostics;
  }

  public void setDiagnostics(NameUsageMatch.Diagnostics diagnostics) {
    this.diagnostics = diagnostics;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    } else if (o != null && this.getClass() == o.getClass()) {
      NameUsageMatch that = (NameUsageMatch)o;
      return this.synonym == that.synonym &&
          Objects.equals(this.usage, that.usage) &&
          Objects.equals(this.acceptedUsage, that.acceptedUsage) &&
          Objects.equals(this.nomenclature, that.nomenclature) &&
          Objects.equals(this.classification, that.classification) &&
          Objects.equals(this.diagnostics, that.diagnostics);
    } else {
      return false;
    }
  }

  @Override
  public int hashCode() {
    return Objects.hash(this.synonym, this.usage, this.acceptedUsage, this.nomenclature, this.classification, this.diagnostics);
  }

  public static class Diagnostics {
    private org.gbif.api.model.checklistbank.NameUsageMatch.MatchType matchType;
    private Integer confidence;
    private TaxonomicStatus status;
    private List<String> lineage = Lists.newArrayList();
    private List<NameUsageMatch> alternatives = Lists.newArrayList();
    private String note;


    public MatchType getMatchType() {
      return this.matchType;
    }

    public void setMatchType(MatchType matchType) {
      this.matchType = matchType;
    }

    public Integer getConfidence() {
      return this.confidence;
    }

    public void setConfidence(Integer confidence) {
      this.confidence = confidence;
    }

    public TaxonomicStatus getStatus() {
      return this.status;
    }

    public void setStatus(TaxonomicStatus status) {
      this.status = status;
    }

    public List<String> getLineage() {
      return this.lineage;
    }

    public void setLineage(List<String> lineage) {
      this.lineage = lineage;
    }

    public List<NameUsageMatch> getAlternatives() {
      return this.alternatives;
    }

    public void setAlternatives(List<NameUsageMatch> alternatives) {
      this.alternatives = alternatives;
    }

    public String getNote() {
      return this.note;
    }

    public void setNote(String note) {
      this.note = note;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) {
        return true;
      } else if (o != null && this.getClass() == o.getClass()) {
        NameUsageMatch.Diagnostics that = (NameUsageMatch.Diagnostics)o;
        return this.status == that.status &&
               this.matchType == that.matchType &&
               Objects.equals(this.confidence, that.confidence) &&
               Objects.equals(this.lineage, that.lineage) &&
               Objects.equals(this.alternatives, that.alternatives) &&
               Objects.equals(this.note, that.note);
      } else {
        return false;
      }
    }

    @Override
    public int hashCode() {
      return Objects.hash(this.status, this.matchType, this.confidence, this.lineage, this.alternatives, this.note);
    }
  }

  public static class Nomenclature {
    private String source;
    private String id;

    public String getSource() {
      return this.source;
    }

    public void setSource(String source) {
      this.source = source;
    }

    public String getId() {
      return this.id;
    }

    public void setId(String id) {
      this.id = id;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) {
        return true;
      } else if (o != null && this.getClass() == o.getClass()) {
        NameUsageMatch.Nomenclature that = (NameUsageMatch.Nomenclature)o;
        return Objects.equals(this.source, that.source) && Objects.equals(this.id, that.id);
      } else {
        return false;
      }
    }

    @Override
    public int hashCode() {
      return Objects.hash(this.source, this.id);
    }
  }
}
