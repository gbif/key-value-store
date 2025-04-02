# KVS Common

This module contains the main interfaces used by KV stores.

A [KeyValueStore](src/main/java/org/gbif/kvs/KeyValueStore.java) 
is a generic store that allow the retrieval of data based on a known key.
This interface does not impose a way of how the elements are stored, some implementation 
might decide to be read-only while others can  implement an internal lookup mechanism 
in the `get` method to allow a transparent and incremental data loading.

This module has very limited dependencies, allowing it to be used in any project without
introducing a lot of transitive dependencies.