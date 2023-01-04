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

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * Exponential backoff randomized retry.
 */
@Data
@AllArgsConstructor
public class LoaderRetryConfig {

  private static final int DEFAULT_MAX_ATTEMPTS = 3;
  private static final long DEFAULT_INITIAL_INTERVAL = 500;
  private static final double DEFAULT_MULTIPLIER = 1.5;
  private static final double DEFAULT_RANDOMIZATION_FACTOR = 0.5;

  public static LoaderRetryConfig DEFAULT = new LoaderRetryConfig();

  /**
   * Maximum number of attempts/retries.
   */
  private final Integer maxAttempts;

  /**
   * Initial interval after first retry.
   */
  private final Long initialIntervalMillis;

  /**
   * Exponential backoff multiplier factor.
   */
  private final Double multiplier;

  /**
   * Jitter random factor.
   */
  private final Double randomizationFactor;

  private LoaderRetryConfig() {
    this(DEFAULT_MAX_ATTEMPTS, DEFAULT_INITIAL_INTERVAL, DEFAULT_MULTIPLIER, DEFAULT_RANDOMIZATION_FACTOR);
  }

}
