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
package org.gbif.kvs.indexing.species;

import org.gbif.kvs.indexing.options.HBaseIndexingOptions;

import java.util.Map;

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


  @Description("GBIF NameUsageSearchResponse base API URL")
  String getNameUsageBaseApiUrl();

  void setNameUsageBaseApiUrl(String nameUsageBaseApiUrl);

  @Description("GBIF NameUsageSearchResponse API connection time-out")
  long getNameUsageApiTimeOut();

  void setNameUsageApiTimeOut(long nameUsageApiTimeOut);

  @Description("NameUsageSearchResponse Rest/HTTP client file-cache max size")
  long getNameUsageRestClientCacheMaxSize();

  void setNameUsageRestClientCacheMaxSize(long nameUsageRestClientCacheMaxSize);

  @Description("HBase column qualifier to stored geocode JSON response")
  @Default.String("j")
  String getJsonColumnQualifier();

  void setJsonColumnQualifier(String jsonColumnQualifier);

  @Description("Prefixes to replace before doing the taxon/name ID lookups")
  Map<String,String> getPrefixReplacement();
  void setPrefixReplacement(Map<String,String> prefixReplacement);

  @Description("Mapping of taxon/name ID prefixes to the checklist dataset key they should be looked up againdst")
  Map<String,String> getPrefixToDataset();
  void setPrefixToDataset(Map<String,String> prefixReplacement);
}
