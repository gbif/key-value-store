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
package org.gbif.kvs;

import java.io.Closeable;

/**
 * Store of V data indexed by a key (byte[]).
 *
 * @param <K> type of key elements
 * @param <V> type of elements stored
 */
public interface KeyValueStore<K, V> extends Closeable {

  /**
   * Obtains the associated data/payload to the key parameter, as byte[].
   *
   * @param key identifier of element to be retrieved
   * @return the element associated with key, null otherwise
   */
  V get(K key);

}
