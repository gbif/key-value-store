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

import org.gbif.kvs.KeyValueStore;

import java.io.IOException;

import org.cache2k.Cache;
import org.cache2k.Cache2kBuilder;

/**
 * In-memory cache2 for {@link KeyValueStore}.
 * Wraps an instance of a KeyValueStore in a in-memory cache.
 * @param <K> type of key elements
 * @param <V> type of value elements
 */
public class KeyValueCache<K,V> implements KeyValueStore<K,V> {

  //Wrapped KeyValueStore
  private final KeyValueStore<K,V> keyValueStore;

  //Cache2k instance
  private final Cache<K,V> cache;

  /**
   * Creates a Cache for the KV store.
   * @param keyValueStore wrapped kv store
   * @param capacity maximum capacity of the cache
   * @param keyClass type descriptor for the key elements
   * @param valueClass type descriptor for the value elements
   */
  private KeyValueCache(KeyValueStore<K,V> keyValueStore, long capacity, Class<K> keyClass, Class<V> valueClass) {
    this.keyValueStore = keyValueStore;
    this.cache = Cache2kBuilder.of(keyClass, valueClass)
        .eternal(true)    //never expire entries
        .entryCapacity(capacity) //maximum capacity
        .loader(keyValueStore::get) //auto populating function
        .permitNullValues(true) //allow nulls
        .build();
  }


  /**
   * Factory method to create instances of KeyValueStore caches.
   * @param keyValueStore store to be cached/wrapped
   * @param capacity maximum capacity of the in-memory cache
   * @param keyClass type descriptor for the key elements
   * @param valueClass type descriptor for the value elements
   * @param <K1> type of key elements
   * @param <V1> type of value elements
   * @return a new instance of KeyValueStore cache
   */
  public static <K1,V1> KeyValueStore<K1,V1> cache(KeyValueStore<K1,V1> keyValueStore, long capacity, Class<K1> keyClass, Class<V1> valueClass) {
    return new KeyValueCache<>(keyValueStore, capacity, keyClass, valueClass);
  }


  @Override
  public V get(K key) {
    return cache.get(key);
  }

  @Override
  public void close() throws IOException {
    cache.close();
    keyValueStore.close();
  }

}
