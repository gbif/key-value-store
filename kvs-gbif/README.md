#kvs-gbif

This module contains implementations of GBIF KV stores for fast look-up data.

## Geocode KV store/cache

[GeocodeKVStoreFactory](src/main/java/org/gbif/kvs/geocode/GeocodeKVStoreFactory.java) provides instances of
[HBaseStore](../kvs-core/src/main/java/org/gbif/kvs/hbase/HBaseStore.java) to access and store reverse geocode data.
It uses the Rest [Geocode service client](../kvs-rest-clients/src/main/java/org/gbif/rest/client/geocode/GeocodeService.java) to add data incrementally to the HBase store.

To create an instance of KV store client use:

```
GeocodeKVStoreFactory.simpleGeocodeKVStore(CachedHBaseKVStoreConfiguration.builder()
                                                 .withValueColumnQualifier("c") //stores ISO country code
                                                 .withHBaseKVStoreConfiguration(HBaseKVStoreConfiguration.builder()
                                                                                  .withTableName("geocode_kv") //Geocode KV HBase table
                                                                                  .withColumnFamily("v") //Column in which qualifiers are stored
                                                                                  .withNumOfKeyBuckets(10) //Buckets for salted key generations
                                                                                  .withHBaseZk("zk1.dev.org,zk2.dev.org,zk3.dev.org") //HBase Zookeeper ensemble
                                                                                  .build()).build(),
                                               ClientConfiguration.builder()
                                                 .withBaseApiUrl("https://api.gbif-dev.org/v1/") //GBIF base API url
                                                 .withFileCacheMaxSizeMb(64L) //Max file cache size
                                                 .withTimeOut(60L) //Geocode service connection time-out
                                                 .build());
```

## Taxonomic NameMatch KV store/cache

[GeocodeKVStoreFactory](src/main/java/org/gbif/kvs/species/NameUsageMatchKVStoreFactory.java) provides instances of
[HBaseStore](../kvs-core/src/main/java/org/gbif/kvs/hbase/HBaseStore.java) to access and store name usage match data.
It uses the Rest [NameMatch service client](../key-value-store/kvs-rest-clients/src/main/java/org/gbif/rest/client/species/NameMatchService.java) to add data incrementally to the HBase store.

To create an instance of KV store client use:

```
NameUsageMatchKVStoreFactory.nameUsageMatchKVStore(CachedHBaseKVStoreConfiguration.builder()
            .withJsonColumnQualifier("j") //stores JSON data
            .withHBaseKVStoreConfiguration(HBaseKVStoreConfiguration.builder()
                .withTableName("name_usage_kv") //Geocode KV HBase table
                .withColumnFamily("v") //Column in which qualifiers are stored
                .withNumOfKeyBuckets(10) //Buckets for salted key generations
                .withHBaseZk("zk1.dev.org,zk2.dev.org,zk3.dev.org") //HBase Zookeeper ensemble
                .withCacheCapacity(10_000) //Use an in-memory cache with a maximum of 10K entries
                .build()).build(),
             new NameMatchServiceSyncClient(ClientConfiguration.builder()
                                              .withBaseApiUrl("https://api.gbif.org/v1/") //GBIF base API url
                                              .withFileCacheMaxSizeMb(64L) //Max file cache size
                                              .withTimeOut(60L) //Geocode service connection time-out
                                              .build())
```

## Build

To build, install and run tests, execute the Maven command:

`mvn clean package install -U`