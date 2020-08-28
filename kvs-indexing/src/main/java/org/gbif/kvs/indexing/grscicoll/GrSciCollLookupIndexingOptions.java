package org.gbif.kvs.indexing.grscicoll;

import org.gbif.kvs.indexing.options.HBaseIndexingOptions;

import org.apache.beam.sdk.options.Default;
import org.apache.beam.sdk.options.Description;

public interface GrSciCollLookupIndexingOptions extends HBaseIndexingOptions {

  @Description("HBase column qualifier to stored GrSciColl Lookup JSON response")
  @Default.String("j")
  String getJsonColumnQualifier();

  void setJsonColumnQualifier(String jsonColumnQualifier);
}
