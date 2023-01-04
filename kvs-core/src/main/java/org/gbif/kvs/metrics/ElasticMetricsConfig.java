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
package org.gbif.kvs.metrics;


import io.micrometer.elastic.ElasticConfig;
import lombok.Builder;
import lombok.Data;

/**
 * Micrometer Elastic configuration.
 */
@Data
@Builder(setterPrefix = "with", builderClassName = "Builder")
public class ElasticMetricsConfig implements ElasticConfig {

  /**
   * Host to export metrics to.
   */
  @lombok.Builder.Default
  private String host = "http://localhost:9200";

  /**
   * Index to export metrics to.
   */
  @lombok.Builder.Default
  private String index = "metrics";

  /**
   * Index date format used for rolling indices. Appended to the index name, preceded by
   * a '-'.
   */
  @lombok.Builder.Default
  private String indexDateFormat = "yyyy-MM";

  /**
   * Name of the timestamp field.
   */
  @lombok.Builder.Default
  private String timestampFieldName = "@timestamp";

  /**
   * Whether to create the index automatically if it does not exist.
   */
  @lombok.Builder.Default
  private boolean autoCreateIndex = true;

  /**
   * Login user of the Elastic server.
   */
  @lombok.Builder.Default
  private String userName = "";

  /**
   * Login password of the Elastic server.
   */
  @lombok.Builder.Default
  private String password = "";

  @Override
  public String prefix() {
    return null;
  }

  @Override
  public String get(String key) {
    return null;
  }

}
