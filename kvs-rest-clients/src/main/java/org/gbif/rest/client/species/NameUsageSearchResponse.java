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
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


/**
 * Contains a partial NameUsageSearchResponse mapping, with the fields necessary to lookup concepts within a checklist and locate
 * their equivalent backbone taxon id.
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class NameUsageSearchResponse implements Serializable {
  private static final long serialVersionUID = -3315809636602814017L;
  private List<Result> results;

  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  @JsonIgnoreProperties(ignoreUnknown = true)
  public static class Result {
    private int key;
    private Integer nubKey;
    private String scientificName;
  }
}
