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
package org.gbif.test;

import org.gbif.kvs.KeyValueStore;

import java.io.IOException;
import java.util.Map;

/**
 * Simple KV Store backed by Map.
 */
public class KeyValueMapStore<K,V> implements KeyValueStore<K,V> {

  //Store
  private final Map<K,V> store;

  /**
   * Creates an instance using a Map as store.
   * @param store KV store
   */
  public KeyValueMapStore(Map<K,V> store) {
    this.store = store;
  }

  @Override
  public V get(K key) {
    return store.get(key);
  }

  @Override
  public void close() throws IOException {
    //NOTHING
  }
}
