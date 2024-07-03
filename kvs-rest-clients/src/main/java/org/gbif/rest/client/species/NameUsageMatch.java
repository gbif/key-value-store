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

import org.gbif.nameparser.api.Rank;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Data;
import lombok.ToString;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
@ToString
public class NameUsageMatch implements Serializable {

  private boolean synonym;
  private RankedName usage;
  private RankedName acceptedUsage;
  private List<RankedName> classification = new ArrayList<>();
  private List<NameUsageMatch> alternatives = new ArrayList<>();
  private NameUsageMatch.Diagnostics diagnostics = new NameUsageMatch.Diagnostics();
  /**
   * Status information from external sources like IUCN Red List.
   */
  private List<Status> additionalStatus = new ArrayList<>();

  @Data
  @JsonIgnoreProperties(ignoreUnknown = true)
  public static class Diagnostics {
    private MatchType matchType;
    /**
     * Annotations flags to be added based on the rules of interpretation (not part of species/match response)
     * See https://github.com/gbif/pipelines/issues/217
     */
    private List<String> issues;
    private Integer confidence;
    private String status;
    private String note;
    private List<NameUsageMatch> alternatives;
  }

  @Data
  @JsonIgnoreProperties(ignoreUnknown = true)
  public static class Status {
    private String datasetKey;
    private String gbifKey;
    private String datasetTitle;
    private String category;
  }

  @Data
  public static class RankedName {
      private String key;
      private String name;
      private Rank rank;
  }

  /**
   * This is a copy of the enum taken from ChecklistBank.
   * Deliberately copied to avoid a dependency on ChecklistBank.
   * See link:<a href="https://github.com/CatalogueOfLife/backend/blob/master/api/src/main/java/life/catalogue/api/vocab/MatchType.java">MatchType</a>
   */
  public enum MatchType {
    EXACT,
    VARIANT,
    CANONICAL,
    AMBIGUOUS,
    NONE,
    UNSUPPORTED,
    HIGHERRANK;
  }
}
