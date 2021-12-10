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

import java.util.Collections;
import java.util.List;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tag;

/**
 * Collects metrics about cache usage: hits and inserts.
 */
public class CacheMetrics {

  //Counter of cache hits
  private final Counter hits;

  //Counter of cache inserts or misses
  private final Counter inserts;

  /**
   * Creates a new instance using a hits and a inserts counter.
   * @param hits counter for hits
   * @param inserts counter for misses/inserts
   */
  private CacheMetrics(Counter hits, Counter inserts) {
    this.hits = hits;
    this.inserts = inserts;
  }

  /**
   *
   * @return the number of time a cache instance has been hit
   */
  public Counter getHits() {
    return hits;
  }

  /**
   *
   * @return number of time a cache instance has inserted a new value
   */
  public Counter getInserts() {
    return inserts;
  }

  /**
   * Increments the inserts counter.
   */
  public void incInserts() {
    inserts.increment();
  }

  /**
   * Increments the hits counter.
   */
  public void incHits() {
    hits.increment();
  }

  /**
   * Factory method for CacheMetrics.
   * @param registry meter registry to which the stats are subscribed
   * @param cacheName name of the cache/store
   * @return a new CacheMetrics instance
   */
  public static CacheMetrics create(MeterRegistry registry, String cacheName) {
    List<Tag> metricsTags  = Collections.singletonList(Tag.of("store", cacheName));
    return new CacheMetrics(registry.counter("hits", metricsTags),
                            registry.counter("inserts", metricsTags));
  }

}
