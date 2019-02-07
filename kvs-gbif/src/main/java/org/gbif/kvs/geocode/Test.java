package org.gbif.kvs.geocode;

import org.gbif.kvs.KeyValueStore;
import org.gbif.kvs.hbase.HBaseKVStoreConfiguration;
import org.gbif.rest.client.configuration.ClientConfiguration;

import java.io.IOException;

public class Test {

  public static void main(String[] args) throws IOException {
    KeyValueStore<LatLng, String> store =
    GeocodeKVStoreFactory.simpleGeocodeKVStore(GeocodeKVStoreConfiguration.builder()
        .withJsonColumnQualifier("j") //stores JSON data
        .withCountryCodeColumnQualifier("c") //stores ISO country code
        .withHBaseKVStoreConfiguration(HBaseKVStoreConfiguration.builder()
            .withTableName("geocode_kv2") //Geocode KV HBase table
            .withColumnFamily("v") //Columna in which qualifiers are stored
            .withNumOfKeyBuckets(10) //Buckets for salted key generations
            .withHBaseZk("c3zk1.gbif-dev.org,c3zk2.gbif-dev.org,c3zk3.gbif-dev.org") //HBase Zookeeper ensemble
            .build()).build(),
        ClientConfiguration.builder()
            .withBaseApiUrl("https://api.gbif-uat.org/v1/") //GBIF base API url
            .withFileCacheMaxSizeMb(64L) //Max file cache size
            .withTimeOut(60L) //Geocode service connection time-out
            .build());

    String countryCode = store.get(LatLng.builder().withLatitude(45.0).withLongitude(75.8).build());
    System.out.println(countryCode);
  }
}
