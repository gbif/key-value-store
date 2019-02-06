package org.gbif.kvs.hbase;

import java.nio.charset.Charset;

/** Objects that can be indexed in a HBase KV store. */
public interface Indexable {

  /**
   * Generates a logical key as bytes using an specific encoding.
   * @param charset encoding charset
   * @return a byte[] as a logical key
   */
  byte[] getLogicalKey(Charset charset);
}
