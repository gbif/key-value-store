package org.gbif.kvs;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/**
 * Store of V data indexed by a key (byte[]).
 *
 * @param <K> type of key elements
 * @param <V> type of elements stored
 */
public interface KeyValueStore<K, V> {

  /**
   * Obtains the associated data/payload to the key parameter, as byte[].
   *
   * @param key identifier of element to be retrieved
   * @return the element associated with key, null otherwise
   */
  V get(K key);

  /**
   * Character encoding for byte handling.
   * @return the character encoding used by this store
   */
  default Charset getEncodingCharset() {
    return StandardCharsets.UTF_8;
  }
}
