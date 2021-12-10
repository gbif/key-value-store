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
package org.gbif.rest.client.configuration;

import java.io.Serializable;

import lombok.EqualsAndHashCode;
import lombok.Getter;

@Getter
@EqualsAndHashCode
public class ClientConfiguration implements Serializable {

  private final String baseApiUrl;

  private final Long timeOut;

  private final Long fileCacheMaxSizeMb;

  private ClientConfiguration(String baseApiUrl, long timeOut, long fileCacheMaxSizeMb) {
    this.baseApiUrl = baseApiUrl;
    this.timeOut = timeOut;
    this.fileCacheMaxSizeMb = fileCacheMaxSizeMb;
  }

  /**
   * Creates a new {@link Builder} instance.
   * @return a new builder
   */
  public static Builder builder() {
    return new Builder();
  }

  public static class Builder {

    private String baseApiUrl;
    private Long timeOut = 60L;
    private Long fileCacheMaxSizeMb = 64L;

    /**
     * Hidden constructor to force use the containing class builder() method.
     */
    private Builder() {
      //DO NOTHING
    }


    public Builder withBaseApiUrl(String baseApiUrl) {
      this.baseApiUrl = baseApiUrl;
      return this;
    }

    public Builder withTimeOut(Long timeOut) {
      this.timeOut = timeOut;
      return this;
    }

    public Builder withFileCacheMaxSizeMb(Long fileCacheMaxSizeMb) {
      this.fileCacheMaxSizeMb = fileCacheMaxSizeMb;
      return this;
    }

    public ClientConfiguration build() {
      return new ClientConfiguration(baseApiUrl, timeOut, fileCacheMaxSizeMb);
    }
  }
}
