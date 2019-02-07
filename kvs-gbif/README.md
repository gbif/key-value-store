#kvs-gbif

This module contains implementations of GBIF KV stores for fast look-up data.

## Geocode KV store/cache

[GeocodeKVStoreFactory](src/main/java/org/gbif/kvs/geocode/GeocodeKVStoreFactory.java) provides instances of
[HBaseStore](../kvs-core/src/main/java/org/gbif/kvs/hbase/HBaseStore.java) to access and store reverse geocode data.
It uses the Rest [Geocode service client](../kvs-rest-clients/src/main/java/org/gbif/rest/client/geocode/GeocodeService.java) to add data incrementally to the HBase store.

To create an instance of KV store client use:

```
GeocodeKVStoreFactory.simpleGeocodeKVStore(GeocodeKVStoreConfiguration.builder()
                            .withJsonColumnQualifier("j") //stores JSON data
                            .withCountryCodeColumnQualifier("c") //stores ISO country code
                            .withHBaseKVStoreConfiguration(HBaseKVStoreConfiguration.builder()
                                                            .withTableName("geocode_kv") //Geocode KV HBase table
                                                            .withColumnFamily("v") //Column in which qualifiers are stored
                                                            .withNumOfKeyBuckets(10) //Buckets for salted key generations
                                                            .withHBaseZk("zk1.dev.org,zk2.dev.org,zk3.dev.org") //HBase Zookeeper ensemble
                                                            .build()).build(),
                            ClientConfiguration.builder()
                             .withBaseApiUrl("https://api.gbif.org/v1/") //GBIF base API url
                             .withFileCacheMaxSizeMb(64L) //Max file cache size
                             .withTimeOut(60L) //Geocode service connection time-out
                             .build());
```

## Build

To build, install and run tests, execute the Maven command:

`mvn clean package install -U`