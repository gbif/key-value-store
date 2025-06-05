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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.*;
import lombok.experimental.SuperBuilder;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
@ToString
@SuperBuilder(setterPrefix = "with")
@NoArgsConstructor
@AllArgsConstructor
public class NameUsageMatchResponse implements Serializable {

  private boolean synonym;
  private Usage usage;
  private Usage acceptedUsage;
  private List<RankedName> classification = new ArrayList<>();
  private List<NameUsageMatchResponse> alternatives = new ArrayList<>();
  private NameUsageMatchResponse.Diagnostics diagnostics = new NameUsageMatchResponse.Diagnostics();
  private Long left;
  private Long right;
  /**
   * Status information from external sources like IUCN Red List.
   */
  private List<Status> additionalStatus = new ArrayList<>();

  @Data
  @JsonIgnoreProperties(ignoreUnknown = true)
  @Builder(setterPrefix = "with")
  @NoArgsConstructor
  @AllArgsConstructor
  public static class Diagnostics {
    private MatchType matchType;
    /**
     * Annotation flags to be added based on the rules of interpretation
     * (not part of species/match response)
     * See https://github.com/gbif/pipelines/issues/217
     */
    private List<String> processingFlags = new ArrayList<String>();
    private List<String> issues = new ArrayList<String>();
    private Integer confidence;
    private String note;
    private List<NameUsageMatchResponse> alternatives = new ArrayList<NameUsageMatchResponse>();
  }

  @Data
  @JsonIgnoreProperties(ignoreUnknown = true)
  @Builder(setterPrefix = "with")
  @NoArgsConstructor
  @AllArgsConstructor
  public static class Status {
    private String datasetKey;
    private String gbifKey;
    private String datasetAlias;
    private String status;
    private String statusCode;
    private String sourceId;
  }

  @Data
  @Builder(setterPrefix = "with")
  @NoArgsConstructor
  @AllArgsConstructor
  public static class Usage {
    private String key;
    private String name;
    private String canonicalName;
    private String authorship;
    private String rank;
    private String code;
    private String status;
    private String genericName;
    private String infragenericEpithet;
    private String specificEpithet;
    private String infraspecificEpithet;
    private String type;
    private String formattedName;
  }

  @Data
  @Builder(setterPrefix = "with")
  @NoArgsConstructor
  @AllArgsConstructor
  public static class RankedName {
      private String key;
      private String name;
      private String rank;
      private String code;
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
