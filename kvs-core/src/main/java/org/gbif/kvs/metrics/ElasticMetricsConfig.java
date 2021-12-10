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

import java.util.Objects;

import io.micrometer.elastic.ElasticConfig;

/**
 * Micrometer Elastic configuration.
 */
public class ElasticMetricsConfig implements ElasticConfig {

  /**
   * Host to export metrics to.
   */
  private String host = "http://localhost:9200";

  /**
   * Index to export metrics to.
   */
  private String index = "metrics";

  /**
   * Index date format used for rolling indices. Appended to the index name, preceded by
   * a '-'.
   */
  private String indexDateFormat = "yyyy-MM";

  /**
   * Name of the timestamp field.
   */
  private String timestampFieldName = "@timestamp";

  /**
   * Whether to create the index automatically if it does not exist.
   */
  private boolean autoCreateIndex = true;

  /**
   * Login user of the Elastic server.
   */
  private String userName = "";


  /**
   * Login password of the Elastic server.
   */
  private String password = "";


  /**
   * Full constructor.
   */
  private ElasticMetricsConfig(String host, String index, String indexDateFormat, String timestampFieldName,
                              boolean autoCreateIndex, String userName, String password) {
    this.host = host;
    this.index = index;
    this.indexDateFormat = indexDateFormat;
    this.timestampFieldName = timestampFieldName;
    this.autoCreateIndex = autoCreateIndex;
    this.userName = userName;
    this.password = password;
  }

  @Override
  public String prefix() {
    return null;
  }

  @Override
  public String host() {
    return host;
  }

  @Override
  public String index() {
    return index;
  }

  @Override
  public String indexDateFormat() {
    return indexDateFormat;
  }

  @Override
  public String timestampFieldName() {
    return timestampFieldName;
  }

  @Override
  public boolean autoCreateIndex() {
    return autoCreateIndex;
  }

  @Override
  public String userName() {
    return userName;
  }

  @Override
  public String password() {
    return password;
  }

  @Override
  public String get(String key) {
    return null;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    ElasticMetricsConfig that = (ElasticMetricsConfig) o;
    return autoCreateIndex == that.autoCreateIndex &&
        Objects.equals(host, that.host) &&
        Objects.equals(index, that.index) &&
        Objects.equals(indexDateFormat, that.indexDateFormat) &&
        Objects.equals(timestampFieldName, that.timestampFieldName) &&
        Objects.equals(userName, that.userName) &&
        Objects.equals(password, that.password);
  }

  @Override
  public int hashCode() {
    return Objects.hash(host, index, indexDateFormat, timestampFieldName, autoCreateIndex, userName, password);
  }

  /**
   *
   * @return a new Builder instance.
   */
  public Builder builder() {
    return new Builder();
  }

  /**
   * Configuration builder.
   */
  public static class Builder {
    private String host;
    private String index;
    private String indexDateFormat;
    private String timestampFieldName;
    private boolean autoCreateIndex;
    private String userName;
    private String password;

    public Builder withHost(String host) {
      this.host = host;
      return this;
    }

    public Builder withIndex(String index) {
      this.index = index;
      return this;
    }

    public Builder withIndexDateFormat(String indexDateFormat) {
      this.indexDateFormat = indexDateFormat;
      return this;
    }

    public Builder withTimestampFieldName(String timestampFieldName) {
      this.timestampFieldName = timestampFieldName;
      return this;
    }

    public Builder withAutoCreateIndex(boolean autoCreateIndex) {
      this.autoCreateIndex = autoCreateIndex;
      return this;
    }

    public Builder withUserName(String userName) {
      this.userName = userName;
      return this;
    }

    public Builder withPassword(String password) {
      this.password = password;
      return this;
    }

    public ElasticMetricsConfig createElasticMetricsConfig() {
      return new ElasticMetricsConfig(host, index, indexDateFormat, timestampFieldName, autoCreateIndex, userName, password);
    }
  }
}
