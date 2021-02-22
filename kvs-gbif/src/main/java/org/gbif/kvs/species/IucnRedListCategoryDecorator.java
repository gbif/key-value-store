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
