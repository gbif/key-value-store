package org.gbif.kvs.indexing.species;

import org.gbif.kvs.indexing.options.HBaseIndexingOptions;

import org.apache.beam.sdk.options.Default;
import org.apache.beam.sdk.options.Description;

/** Apache Beam options for indexing into HBase NameUsageMatch lookups. */
public interface NameUsageMatchIndexingOptions extends HBaseIndexingOptions {

  @Description("HBase column qualifier to stored geocode JSON response")
  @Default.String("j")
  String getJsonColumnQualifier();

  void setJsonColumnQualifier(String jsonColumnQualifier);
}
