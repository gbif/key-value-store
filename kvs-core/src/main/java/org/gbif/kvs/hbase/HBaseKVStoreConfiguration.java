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
package org.gbif.kvs.hbase;

import java.io.Serializable;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseConfiguration;

import lombok.Builder;
import lombok.Data;

/** Configuration settings for KV store based on a simple HBase table. */
@Data
@Builder(setterPrefix = "with", builderClassName = "Builder")
public class HBaseKVStoreConfiguration implements Serializable {

  /**
   * HBase Zookeeper ensemble.
   */
  private final String hBaseZk;

  /**
   * HBase Zookeeper root Znode.
   */
  @lombok.Builder.Default
  private final String hBaseZnode = "/hbase";

  /**
   * HBase table name to store key value elements.
   */
  private final String tableName;

  /**
   * Column family in which the values are stored.
   */
  private final String columnFamily;

  /**
   * Number of buckets
   */
  private final int numOfKeyBuckets;

  /**
   * Creates an instance of {@link Configuration} setting the 'hbase.zookeeper.quorum' to
   * getHbaseZk and zookeeper.znode.parent to hBaseZnode.
   *
   * @return a new Hadoop configuration
   */
  public Configuration hbaseConfig() {
    Configuration hbaseConfig = HBaseConfiguration.create();
    hbaseConfig.set("hbase.zookeeper.quorum", hBaseZk);
    hbaseConfig.set("zookeeper.znode.parent", hBaseZnode);
    return hbaseConfig;
  }
}
