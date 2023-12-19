package org.gbif.kvs.wf;

import org.apache.beam.sdk.options.PipelineOptionsFactory;

import org.gbif.kvs.indexing.grscicoll.GrSciCollLookupIndexingOptions;
import org.gbif.kvs.wf.trino.ConnectionConfiguration;
import org.gbif.kvs.wf.trino.QueryUtils;
import org.gbif.kvs.wf.trino.TrinoConfiguration;

import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Objects;

import org.apache.commons.io.IOUtils;

import lombok.Builder;
import lombok.SneakyThrows;

public class GrscicollCacheWorkflow {

  TrinoConfiguration trinoConfiguration;

  @Builder
  public GrscicollCacheWorkflow(TrinoConfiguration trinoConfiguration) {
    this.trinoConfiguration = trinoConfiguration;
  }

  public static void main(String[] args) throws SQLException {
    ConnectionConfiguration configuration =
        ConnectionConfiguration.builder()
            .user("gbif")
            .password("gbifP2023")
            .url("jdbc:trino://130.226.238.141:31843/hive/marcos")
            .build();

    Connection connection = QueryUtils.getConnection(configuration);
    ResultSet rs = connection.createStatement().executeQuery("select * from raw limit 10");
  }

  public void run() {
    // create trino table with all the possible combinations for the ws lookup
    createTrinoTable();

    // remove obsolete values in the cache
    /*GrSciCollLookupIndexingOptions options =
      PipelineOptionsFactory.fromArgs(args)
        .withValidation()
        .as(GrSciCollLookupIndexingOptions.class);
    run(options);*/

  }

  @SneakyThrows
  private void createTrinoTable() {
    String query =
        IOUtils.toString(
                Objects.requireNonNull(
                    getClass()
                        .getResourceAsStream("create-trino-table.q")),
                StandardCharsets.UTF_8)
            .replace("${targetTable}", trinoConfiguration.getTargetTable())
            .replace("${sourceTable}", trinoConfiguration.getSourceTable());

    QueryUtils.runInTrino(trinoConfiguration.getConnectionConfiguration(), query);
  }
}
