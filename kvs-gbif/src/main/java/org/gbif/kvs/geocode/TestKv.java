package org.gbif.kvs.geocode;

import org.gbif.kvs.KeyValueStore;
import org.gbif.kvs.conf.CachedHBaseKVStoreConfiguration;
import org.gbif.kvs.hbase.HBaseKVStoreConfiguration;
import org.gbif.rest.client.configuration.ClientConfiguration;
import org.gbif.rest.client.geocode.GeocodeResponse;

public class TestKv {

    public static void main(String[] args) throws Exception {
        KeyValueStore<LatLng, GeocodeResponse> kv =
        GeocodeKVStoreFactory.simpleGeocodeKVStore(CachedHBaseKVStoreConfiguration.builder()
                        .withValueColumnQualifier("v") //stores JSON data
                        .withHBaseKVStoreConfiguration(HBaseKVStoreConfiguration.builder()
                                .withTableName("geocode_kv") //Geocode KV HBase table
                                .withColumnFamily("v") //Column in which qualifiers are stored
                                .withNumOfKeyBuckets(10) //Buckets for salted key generations
                                .withHBaseZk("c3master1-vh.gbif.org,c3master2-vh.gbif.org,c3master3-vh.gbif.org") //HBase Zookeeper ensemble
                                .build()).build(),
                ClientConfiguration.builder()
                        .withBaseApiUrl("https://api.gbif.org/v1/") //GBIF base API url
                        .withFileCacheMaxSizeMb(64L) //Max file cache size
                        .withTimeOut(60L) //Geocode service connection time-out
                        .build());
    GeocodeResponse response = kv.get(LatLng.create(37.51,-79.51));
    System.out.println(response);
    }
}
