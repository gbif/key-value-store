package org.gbif.kvs.hbase;

/**
 * Single method interface for convert objects into logical keys.
 */
public interface LogicalKey {

    /**
     * Generates a logical key.
     * @return a byte[] as a logical key
     */
    byte[] getLogicalKey();
}
