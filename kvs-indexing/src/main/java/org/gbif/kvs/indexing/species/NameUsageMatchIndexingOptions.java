package org.gbif.kvs.indexing.species;

import org.gbif.kvs.indexing.options.HBaseIndexingOptions;

import org.apache.beam.sdk.options.Default;
import org.apache.beam.sdk.options.Description;

/** Apache Beam options for indexing into HBase NameUsageMatch lookups. */
public interface NameUsageMatchIndexingOptions extends HBaseIndexingOptions {

  @Description("GBIF Checklistbank base API URL")
  String getClbBaseApiUrl();

  void setClbBaseApiUrl(String clbBaseApiUrl);

  @Description("GBIF Checklistbank API connection time-out")
  long getClbApiTimeOut();

  void setClbApiTimeOut(long clbApiTimeOut);

  @Description("Checklistbank Rest/HTTP client file-cache max size")
  long getClbRestClientCacheMaxSize();

  void setClbRestClientCacheMaxSize(long clbRestClientCacheMaxSize);


  @Description("GBIF NameUsage base API URL")
  String getNameUsageBaseApiUrl();

  void setNameUsageBaseApiUrl(String nameUsageBaseApiUrl);

  @Description("GBIF NameUsage API connection time-out")
  long getNameUsageApiTimeOut();

  void setNameUsageApiTimeOut(long nameUsageApiTimeOut);

  @Description("NameUsage Rest/HTTP client file-cache max size")
  long getNameUsageRestClientCacheMaxSize();

  void setNameUsageRestClientCacheMaxSize(long nameUsageRestClientCacheMaxSize);

  @Description("HBase column qualifier to stored geocode JSON response")
  @Default.String("j")
  String getJsonColumnQualifier();

  void setJsonColumnQualifier(String jsonColumnQualifier);
}
