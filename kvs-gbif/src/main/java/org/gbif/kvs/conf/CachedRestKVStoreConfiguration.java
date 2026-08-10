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
package org.gbif.kvs.conf;

import java.io.Serializable;
import lombok.Builder;
import lombok.Getter;
import org.gbif.kvs.hbase.LoaderRetryConfig;

/** Configuration settings to create a KV Store/Cache for the GBIF species name match. */
@Getter
@Builder(setterPrefix = "with", builderClassName = "Builder")
public class CachedRestKVStoreConfiguration implements Serializable {

  // Exponential backoff loader retry config
  private final LoaderRetryConfig loaderRetryConfig;

  // Stores the entire JSON response of the Geocode service
  private final String valueColumnQualifier;

  private final Long cacheCapacity;

  private final Long cacheExpiryTimeInSeconds;

  /**
   * Creates an configuration instance using the HBase KV and Rest client configurations.
   *
   * @param loaderRetryConfig exponential backoff retry config
   * @param valueColumnQualifier column qualifier to store the entire json response
   * @param cacheCapacity maximum number of entries in the in-memory cache
   * @param cacheExpiryTimeInSeconds cache expiry time in seconds
   */
  public CachedRestKVStoreConfiguration(
      LoaderRetryConfig loaderRetryConfig,
      String valueColumnQualifier,
      Long cacheCapacity,
      Long cacheExpiryTimeInSeconds) {
    this.loaderRetryConfig = loaderRetryConfig;
    this.valueColumnQualifier = valueColumnQualifier;
    this.cacheCapacity = cacheCapacity;
    this.cacheExpiryTimeInSeconds = cacheExpiryTimeInSeconds;
  }
}
