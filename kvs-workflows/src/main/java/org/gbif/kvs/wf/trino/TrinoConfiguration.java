package org.gbif.kvs.wf.trino;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TrinoConfiguration {

  private final ConnectionConfiguration connectionConfiguration;
  private final String dbName;
  private final String sourceTable;
  private final String targetTable;
}
