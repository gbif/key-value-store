package org.gbif.rest.client.species;

import java.io.Serializable;
import java.util.List;

import org.gbif.api.v2.RankedName;
import org.gbif.api.vocabulary.TaxonomicStatus;

import com.google.common.collect.Lists;
import lombok.Data;

@Data
public class NameUsageMatch implements Serializable {

  private boolean synonym;
  private RankedName usage;
  private RankedName acceptedUsage;
  private NameUsageMatch.Nomenclature nomenclature;
  private List<RankedName> classification = Lists.newArrayList();
  private NameUsageMatch.Diagnostics diagnostics = new NameUsageMatch.Diagnostics();

  @Data
  public static class Diagnostics {
    private org.gbif.api.model.checklistbank.NameUsageMatch.MatchType matchType;
    private Integer confidence;
    private TaxonomicStatus status;
    private List<String> lineage = Lists.newArrayList();
    private List<NameUsageMatch> alternatives = Lists.newArrayList();
    private String note;
  }

  @Data
  public static class Nomenclature {
    private String source;
    private String id;
  }
}
