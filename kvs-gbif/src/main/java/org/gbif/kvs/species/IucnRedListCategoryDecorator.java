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
package org.gbif.kvs.species;

import org.gbif.rest.client.species.ChecklistbankService;
import org.gbif.rest.client.species.NameUsageMatch;

import java.util.Objects;

import lombok.AllArgsConstructor;

@AllArgsConstructor(staticName = "with")
public class IucnRedListCategoryDecorator {

  private ChecklistbankService checklistbankService;

  /**
   * Retrieves the IUCN Red List category from the name usage match response.
   */
  public NameUsageMatch decorate(NameUsageMatch nameUsageMatch) {
    Integer usageKey = getUsage(nameUsageMatch);
    if (Objects.nonNull(usageKey)) {
      nameUsageMatch.setIucnRedListCategory(checklistbankService.getIucnRedListCategory(usageKey));
    }
    return nameUsageMatch;
  }

  /**
   * Gets the first non-null value between the accepted usage and the usage of a NameUsageMatch.
   */
  private Integer getUsage(NameUsageMatch nameUsageMatch) {
    return Objects.nonNull(nameUsageMatch.getUsage())? nameUsageMatch.getUsage().getKey(): null;
  }
}
