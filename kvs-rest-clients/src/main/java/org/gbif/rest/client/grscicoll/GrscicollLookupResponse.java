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
package org.gbif.rest.client.grscicoll;

import org.gbif.api.model.collections.lookup.Match.MatchType;
import org.gbif.api.model.collections.lookup.Match.Reason;

import java.io.Serializable;
import java.util.Set;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Data;

import static org.gbif.api.model.collections.lookup.Match.Status;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class GrscicollLookupResponse implements Serializable {

  private Match institutionMatch;
  private Match collectionMatch;

  @Data
  @JsonIgnoreProperties(ignoreUnknown = true)
  public static class Match implements Serializable {
    private MatchType matchType;
    private Status status;
    private Set<Reason> reasons;
    private EntityMatchedResponse entityMatched;
  }

  @Data
  @JsonIgnoreProperties(ignoreUnknown = true)
  public static class EntityMatchedResponse implements Serializable {
    private UUID key;
  }

}
