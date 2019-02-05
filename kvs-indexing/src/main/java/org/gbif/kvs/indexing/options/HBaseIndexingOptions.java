package org.gbif.kvs.indexing.options;

import org.apache.beam.sdk.options.Default;
import org.apache.beam.sdk.options.Description;
import org.apache.beam.sdk.options.PipelineOptions;

/** Beam options/configuration setting for the the HBase indexer. */
public interface HBaseIndexingOptions extends PipelineOptions {

  @Description("HBase Zookeeper ensemble")
  String getHbaseZk();

  void setHbaseZk(String hbaseZk);

  @Description("HBase input table to be scan to extract values")
  String getSourceTable();

  void setSourceTable(String sourceTable);

  @Description("HBase table to store key-value elements")
  String getTargetTable();

  void setTargetTable(String targetTable);

  @Description("HBase column family in which the elements are stored")
  @Default.String("v")
  String getKVColumnFamily();

  void setKVColumnFamily(String kvColumnFamily);

  @Description("GBIF base API url")
  String getBaseApiUrl();

  void setBaseApiUrl(String baseApiUrl);

  @Description("Number of buckets/split to generate in salted-keys")
  int getSaltedKeyBuckets();

  void setSaltedKeyBuckets(int saltedKeyBuckets);

  @Description("GBIF API connection time-out")
  long getApiTimeOut();

  void setApiTimeOut(long apiTimeOut);

  @Description("Rest/Http client file-cache max size")
  long getRestClientCacheMaxSize();

  void setRestClientCacheMaxSize(long restClientCacheMaxSize);
}
