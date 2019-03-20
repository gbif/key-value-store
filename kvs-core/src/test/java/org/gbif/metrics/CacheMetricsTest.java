package org.gbif.metrics;

import org.gbif.kvs.metrics.CacheMetrics;

import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.Test;

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
