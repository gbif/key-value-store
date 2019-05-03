package org.gbif.kvs.hbase;

/**
 * Functional interface that receives no parameters and returns nothing.
 */
@FunctionalInterface
public interface Command {
    void execute();
}
