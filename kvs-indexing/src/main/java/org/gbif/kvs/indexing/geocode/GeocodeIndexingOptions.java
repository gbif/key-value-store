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
package org.gbif.kvs.indexing.geocode;

import org.gbif.kvs.hbase.HBaseKVStoreConfiguration;
import org.gbif.rest.client.configuration.ClientConfiguration;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import lombok.Builder;
import lombok.Value;

/**
 * Options for the {@link ReverseGeocodeIndexer}, parsed from {@code --name=value} arguments.
 */
@Value
@Builder
public class GeocodeIndexingOptions implements Serializable {

  /** Table to read the coordinates from, e.g. {@code occurrence} or {@code catalog.db.occurrence}. */
  @Builder.Default String sourceTable = "occurrence";

  /** Optional number of partitions, controls the number of concurrent geocode API callers. */
  Integer numPartitions;

  String hbaseZk;
  String hbaseZkNode;
  String targetTable;
  @Builder.Default String kvColumnFamily = "v";
  @Builder.Default String jsonColumnQualifier = "j";
  int saltedKeyBuckets;

  String baseApiUrl;
  /** API time-out in seconds. */
  @Builder.Default int apiTimeOut = 60;
  /** REST client file-cache max size in MB. */
  @Builder.Default long restClientCacheMaxSize = 64;

  /** How often, in seconds, the driver and each partition log lookup progress. */
  @Builder.Default int progressIntervalSeconds = 60;

  public HBaseKVStoreConfiguration hbaseKVStoreConfiguration() {
    return HBaseKVStoreConfiguration.builder()
        .withTableName(targetTable)
        .withColumnFamily(kvColumnFamily)
        .withHBaseZk(hbaseZk)
        .withHBaseZnode(hbaseZkNode)
        .withNumOfKeyBuckets(saltedKeyBuckets)
        .build();
  }

  public ClientConfiguration clientConfiguration() {
    return ClientConfiguration.builder()
        .withBaseApiUrl(baseApiUrl)
        .withTimeOutMillisec(apiTimeOut * 1000)
        .withFileCacheMaxSizeMb(restClientCacheMaxSize)
        .build();
  }

  /** Parses {@code --name=value} arguments, failing on unknown or missing required ones. */
  public static GeocodeIndexingOptions fromArgs(String[] args) {
    Map<String, String> values = new HashMap<>();
    for (String arg : args) {
      if (!arg.startsWith("--") || !arg.contains("=")) {
        throw new IllegalArgumentException("Expected --name=value but got: " + arg);
      }
      int eq = arg.indexOf('=');
      values.put(arg.substring(2, eq), arg.substring(eq + 1));
    }

    GeocodeIndexingOptionsBuilder builder = builder();
    for (Map.Entry<String, String> e : values.entrySet()) {
      String v = e.getValue();
      switch (e.getKey()) {
        case "sourceTable": builder.sourceTable(v); break;
        case "numPartitions": builder.numPartitions(Integer.parseInt(v)); break;
        case "hbaseZk": builder.hbaseZk(v); break;
        case "hbaseZkNode": builder.hbaseZkNode(v); break;
        case "targetTable": builder.targetTable(v); break;
        case "kvColumnFamily": builder.kvColumnFamily(v); break;
        case "jsonColumnQualifier": builder.jsonColumnQualifier(v); break;
        case "saltedKeyBuckets": builder.saltedKeyBuckets(Integer.parseInt(v)); break;
        case "baseApiUrl": builder.baseApiUrl(v); break;
        case "apiTimeOut": builder.apiTimeOut(Integer.parseInt(v)); break;
        case "restClientCacheMaxSize": builder.restClientCacheMaxSize(Long.parseLong(v)); break;
        case "progressIntervalSeconds": builder.progressIntervalSeconds(Integer.parseInt(v)); break;
        default: throw new IllegalArgumentException("Unknown argument: --" + e.getKey());
      }
    }

    GeocodeIndexingOptions options = builder.build();
    Objects.requireNonNull(options.hbaseZk, "--hbaseZk is required");
    Objects.requireNonNull(options.targetTable, "--targetTable is required");
    Objects.requireNonNull(options.baseApiUrl, "--baseApiUrl is required");
    if (options.saltedKeyBuckets <= 0) {
      throw new IllegalArgumentException("--saltedKeyBuckets must be a positive number");
    }
    if (options.progressIntervalSeconds <= 0) {
      throw new IllegalArgumentException("--progressIntervalSeconds must be a positive number");
    }
    return options;
  }
}
