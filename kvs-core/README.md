# kvs-core

This module has concrete implementations of the [KeyValueStore](src/main/java/org/gbif/kvs/KeyValueStore.java) interface
based on HBase, Caffeine and Cache2k.

All the implementations use the [SaltedKeyGenerator](src/main/java/org/gbif/kvs/SaltedKeyGenerator.java) to provide a consistent distributed key in a cluster environment.

In the package [hbase](src/main/java/org/gbif/kvs/hbase), an implementation based on [Apache HBase](https://hbase.apache.org/) is provided.
The [HBaseStore](src/main/java/org/gbif/kvs/hbase/HBaseStore.java) implementation allows a more complex and flexible way of looking up and loading data incrementally.
In particular, a `loader' function has to be provided which is used internally to retrieve values from external sources and store them in the KV store.


## HBase table

It is recommended to enable bloom filters, snappy compression and fast diff for data block enconding.
The number of SPLITS must reflect the number of buckets used for the salted ket, for example if the number of bucket is 10, the HBase table must be created using the following command:

```
create 'geocode_kv', {NAME => 'v', BLOOMFILTER => 'ROW', DATA_BLOCK_ENCODING => 'FAST_DIFF', COMPRESSION => 'SNAPPY'},{SPLITS => ['1','2','3','4','5','6','7','8','9']}
```

## Build

To build, install and run tests, execute the Maven command:

`mvn clean package install -U`