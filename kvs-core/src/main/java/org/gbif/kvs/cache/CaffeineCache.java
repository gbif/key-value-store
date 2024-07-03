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
package org.gbif.kvs.cache;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import org.gbif.kvs.KeyValueStore;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * In-memory caffeine cache for {@link KeyValueStore}. Wraps an instance of a KeyValueStore in a
 * in-memory cache.
 *
 * @param <K> type of key elements
 * @param <V> type of value elements
 */
public class CaffeineCache<K, V> implements KeyValueStore<K, V> {

  // Wrapped KeyValueStore
  private final KeyValueStore<K, V> keyValueStore;

  private final LoadingCache<K, V> cache;

  /**
   * Creates a Cache for the KV store.
   *
   * @param keyValueStore wrapped kv store
   */
  private CaffeineCache(KeyValueStore<K, V> keyValueStore, int capacity, long expiryTimeInSeconds) {
    this.keyValueStore = keyValueStore;
    this.cache =
        Caffeine.newBuilder()
            // Set a fixed time to expire after the last write or access.
            .expireAfterWrite(expiryTimeInSeconds, TimeUnit.SECONDS)
            // The maximum of cached entries
            .maximumSize(capacity)
            .build(keyValueStore::get);
  }

  /**
   * Factory method to create instances of CaffeineCache caches.
   *
   * @param keyValueStore store to be cached/wrapped
   * @param capacity maximum capacity of the in-memory cache
   * @param expiryTimeInSeconds expiry time in seconds
   * @return a new instance of KeyValueStore cache
   */
  public static <K1, V1> KeyValueStore<K1, V1> cache(
      KeyValueStore<K1, V1> keyValueStore, int capacity, long expiryTimeInSeconds) {
    return new CaffeineCache<>(keyValueStore, capacity, expiryTimeInSeconds);
  }

  /**
   * Factory method to create instances of CaffeineCache caches.
   *
   * @param keyValueStore store to be cached/wrapped
   * @return a new instance of KeyValueStore cache
   */
  public static <K1, V1> KeyValueStore<K1, V1> cache(KeyValueStore<K1, V1> keyValueStore) {
    return new CaffeineCache<>(keyValueStore, 1000, Long.MAX_VALUE);
  }

  @Override
  public V get(K key) {
    return cache.get(key);
  }

  @Override
  public void close() throws IOException {
    cache.cleanUp();
    keyValueStore.close();
  }
}
