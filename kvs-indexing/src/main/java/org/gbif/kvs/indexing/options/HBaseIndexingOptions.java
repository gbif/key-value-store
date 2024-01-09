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
package org.gbif.kvs.indexing.options;

import org.apache.beam.sdk.options.Default;
import org.apache.beam.sdk.options.Description;
import org.apache.beam.sdk.options.PipelineOptions;

/** Beam options/configuration setting for the the HBase indexer. */
public interface HBaseIndexingOptions extends PipelineOptions {

  @Description("HBase Zookeeper ensemble")
  String getHbaseZk();

  void setHbaseZk(String hbaseZk);

  @Description("HBase Zookeeper node")
  String getHbaseZkNode();

  void setHbaseZkNode(String hbaseZkNode);

  @Description("Avro source glob to scan and extract values")
  String getSourceGlob();

  void setSourceGlob(String sourceGlob);

  @Description("HBase table to store key-value elements")
  String getTargetTable();

  void setTargetTable(String targetTable);

  @Description("HBase column family in which the elements are stored")
  @Default.String("v")
  String getKVColumnFamily();

  void setKVColumnFamily(String kvColumnFamily);

  @Description("GBIF base API URL")
  String getBaseApiUrl();

  void setBaseApiUrl(String baseApiUrl);

  @Description("Number of buckets/split to generate in salted-keys")
  int getSaltedKeyBuckets();

  void setSaltedKeyBuckets(int saltedKeyBuckets);

  @Description("GBIF API connection time-out")
  long getApiTimeOut();

  void setApiTimeOut(long apiTimeOut);

  @Description("Rest/HTTP client file-cache max size")
  long getRestClientCacheMaxSize();

  void setRestClientCacheMaxSize(long restClientCacheMaxSize);
}
