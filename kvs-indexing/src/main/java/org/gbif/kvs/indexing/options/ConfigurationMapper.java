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

import org.gbif.kvs.hbase.HBaseKVStoreConfiguration;
import org.gbif.rest.client.configuration.ClientConfiguration;

/** Converts common Beam pipeline options into configuration classes. */
public class ConfigurationMapper {

  /** Hidden constructor. */
  private ConfigurationMapper() {
    // DO NOTHING
  }

  /**
   * Creates a {@link HBaseKVStoreConfiguration} from a {@link HBaseIndexingOptions} instance.
   *
   * @param options pipeline options
   * @return a new instance of CachedHBaseKVStoreConfiguration
   */
  public static HBaseKVStoreConfiguration hbaseKVStoreConfiguration(HBaseIndexingOptions options) {
    return HBaseKVStoreConfiguration.builder()
            .withTableName(options.getTargetTable())
            .withColumnFamily(options.getKVColumnFamily())
            .withHBaseZk(options.getHbaseZk())
            .withHBaseZnode(options.getHbaseZkNode())
            .withNumOfKeyBuckets(options.getSaltedKeyBuckets())
            .build();
  }

  /**
   * Creates a {@link ClientConfiguration} from a {@link HBaseIndexingOptions} instance.
   *
   * @param options pipeline options
   * @return a new instance of ClientConfiguration
   */
  public static ClientConfiguration clientConfiguration(HBaseIndexingOptions options) {
    return ClientConfiguration.builder()
            .withBaseApiUrl(options.getBaseApiUrl())
            .withTimeOutMillisec(options.getApiTimeOut() * 1000)
            .withFileCacheMaxSizeMb(options.getRestClientCacheMaxSize())
            .build();
  }
}
