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
