package org.gbif.rest.client.species;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.gbif.api.v2.RankedName;
import org.gbif.api.vocabulary.TaxonomicStatus;
import org.gbif.api.vocabulary.ThreatStatus;

import lombok.Data;

@Data
public class NameUsageMatch implements Serializable {

  private boolean synonym;
  private RankedName usage;
  private RankedName acceptedUsage;
  private NameUsageMatch.Nomenclature nomenclature;
  private List<RankedName> classification = new ArrayList<>();
  private NameUsageMatch.Diagnostics diagnostics = new NameUsageMatch.Diagnostics();

  //This is not part of the NameUsageMatch response, but it is stored in the same record in the Cache
  private ThreatStatus iucnRedListCategory;

  @Data
  public static class Diagnostics {
    private org.gbif.api.model.checklistbank.NameUsageMatch.MatchType matchType;
    private Integer confidence;
    private TaxonomicStatus status;
    private List<String> lineage = new ArrayList<>();
    private List<NameUsageMatch> alternatives = new ArrayList<>();
    private String note;
  }

  @Data
  public static class Nomenclature {
    private String source;
    private String id;
  }
}
