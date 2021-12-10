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
package org.gbif.metrics;

import org.gbif.kvs.metrics.CacheMetrics;

import org.junit.Test;

import io.micrometer.core.instrument.simple.SimpleMeterRegistry;

import static org.junit.Assert.assertEquals;

/**
 * Test cases for class {@link org.gbif.kvs.metrics.CacheMetrics}.
 */
public class CacheMetricsTest {


  /**
   * Validates that the increments for Hits and Insert work using a {@link SimpleMeterRegistry}.
   */
  @Test
  public void incTest() {
    CacheMetrics cacheMetrics = CacheMetrics.create(new SimpleMeterRegistry(), "TestCache");
    double delta = 0.0001; //for comparisons
    cacheMetrics.incHits();
    cacheMetrics.incInserts();
    assertEquals(1, cacheMetrics.getHits().count(), delta);
    assertEquals(1, cacheMetrics.getInserts().count(), delta);
  }


}
