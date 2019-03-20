package org.gbif.cache;

import org.gbif.kvs.KeyValueStore;
import org.gbif.kvs.cache.KeyValueCache;
import org.gbif.test.KeyValueMapStore;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.IntStream;

import org.junit.Assert;
import org.junit.Test;

/**
 * Test cases for the class {@link org.gbif.kvs.cache.KeyValueCache}.
 */
public class KeyValueCacheTest {


  /**
   * Tests the Get operation on {@link KeyValueCache} that wraps a simple KV store backed by a HashMap.
   */
  @Test
  public void getCacheTest() {
    int size = 3;
    Map<String, String> store = new HashMap<>();
    IntStream.rangeClosed(1, size).forEach( val -> store.put("K" + val, "V" + val));
    KeyValueStore<String,String> cache = KeyValueCache.cache(new KeyValueMapStore<>(store), size,
                                                             String.class, String.class);
    IntStream.rangeClosed(1, size).forEach( val -> Assert.assertEquals("V" + val, cache.get("K" + val)));
  }

}
