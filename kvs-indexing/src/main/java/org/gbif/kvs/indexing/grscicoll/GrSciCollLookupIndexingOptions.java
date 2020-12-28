package org.gbif.kvs.indexing.grscicoll;

import org.gbif.kvs.indexing.options.HBaseIndexingOptions;

import org.apache.beam.sdk.options.Default;
import org.apache.beam.sdk.options.Description;

public interface GrSciCollLookupIndexingOptions extends HBaseIndexingOptions {

  @Description("HBase column qualifier to stored GrSciColl Lookup JSON response")
  @Default.String("j")
  String getJsonColumnQualifier();

  void setJsonColumnQualifier(String jsonColumnQualifier);

  @Description("Hive database")
  String getDatabase();

  void setDatabase(String database);

  @Description("Source table with classifications (see project readme)")
  @Default.String("occurrence_collections")
  String getTable();

  void setTable(String table);

  @Description("Uri to hive Metastore, e.g.: thrift://hivesever2:9083")
  String getMetastoreUris();

  void setMetastoreUris(String metastoreUris);
}
