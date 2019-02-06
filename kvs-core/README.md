#kvs-core
This module contains the main abstractions and utilities use by KV stores.

A [KeyValueStore](src/main/java/org/gbif/kvs/KeyValueStore.java) is generic stores that allow the retrieval of data based on a known key.
This interface does not impose a way of how the elements are stored, some implementation might decide to be read-only while others can
implement an internal lookup mechanism in the `get` method to allow a transparent and incremental data loading.

All the implementation are assumed to use the (SaltedKeyGenerator)[src/main/java/org/gbif/kvs/SaltedKeyGenerator.java] to provide a consistent distributed key in a cluster environment.

In the package [hbase](src/main/java/org/gbif/kvs/hbase), an implementation based on [Apache HBase](https://hbase.apache.org/) is provided.
The [HBaseStore](src/main/java/org/gbif/kvs/hbase/HBaseStore.java) implementation allows a more complex and flexible way of looking up and loading data incrementally.
In particular, a `loader' function has to be provided which is used internally to retrieve values from external sources and store them in the KV store.


## HBase table

It is recommended to enable bloom filters, snappy compression and fast diff for data block enconding.

```
create 'geocode_kv', {NAME => 'v', BLOOMFILTER => 'ROW', DATA_BLOCK_ENCODING => 'FAST_DIFF', COMPRESSION => 'SNAPPY'}
```

## Build

To build, install and run tests, execute the Maven command:

`mvn clean package install -U`