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
