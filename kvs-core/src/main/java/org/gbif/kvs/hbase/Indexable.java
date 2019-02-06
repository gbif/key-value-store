package org.gbif.kvs.hbase;

/** Objects that can be indexed in a HBase KV store. */
public interface Indexable {

  /**
   * Generates a logical key as a String.
   * @return a byte[] as a logical key
   */
  String getLogicalKey();
}
