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
import org.gbif.kvs.cache.CaffeineCache;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;

/** Test cases for the class {@link CaffeineCache}. */
public class CaffeineCacheTest {

  /**
   * Tests the Get operation on {@link CaffeineCache} that wraps a simple KV store backed by
   * Caffeine cache.
   */
  @Test
  public void getCacheTest() {
    KeyValueStore<String, String> mockKvs =
        new KeyValueStore<String, String>() {
          @Override
          public String get(String key) {
            return key.toUpperCase();
          }

          @Override
          public void close() throws IOException {}
        };

    KeyValueStore<String, String> cache = CaffeineCache.cache(mockKvs);
    Assert.assertEquals("A", cache.get("a"));
    Assert.assertEquals("B", cache.get("b"));
    Assert.assertEquals("C", cache.get("c"));
  }
}
