package org.gbif.rest.client.configuration;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ChecklistbankClientsConfiguration {

  private final ClientConfiguration checklistbankClientConfiguration;

  private final ClientConfiguration nameUSageClientConfiguration;

}
