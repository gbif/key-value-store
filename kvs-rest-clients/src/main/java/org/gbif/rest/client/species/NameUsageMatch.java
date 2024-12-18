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
package org.gbif.rest.client.species;

import org.gbif.api.v2.RankedName;
import org.gbif.api.vocabulary.OccurrenceIssue;
import org.gbif.api.vocabulary.TaxonomicStatus;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class NameUsageMatch implements Serializable {

  private boolean synonym;
  private RankedName usage;
  private RankedName acceptedUsage;
  private NameUsageMatch.Nomenclature nomenclature;
  private List<RankedName> classification = new ArrayList<>();
  private NameUsageMatch.Diagnostics diagnostics = new NameUsageMatch.Diagnostics();

  // This is not part of the NameUsageMatch response, but it is stored in the same record in the Cache
  private IucnRedListCategory iucnRedListCategory;

  // Annotations flags to be added based on the rules of interpretation (not part of species/match response)
  // See https://github.com/gbif/pipelines/issues/217
  private Set<OccurrenceIssue> issues = new HashSet<>();

  @Data
  @JsonIgnoreProperties(ignoreUnknown = true)
  public static class Diagnostics {
    private org.gbif.api.model.checklistbank.NameUsageMatch.MatchType matchType;
    private Integer confidence;
    private TaxonomicStatus status;
    private List<String> lineage = new ArrayList<>();
    private List<NameUsageMatch> alternatives = new ArrayList<>();
    private String note;
  }

  @Data
  @JsonIgnoreProperties(ignoreUnknown = true)
  public static class Nomenclature {
    private String source;
    private String id;
  }
}
