package org.gbif.kvs.geocode;

import org.gbif.kvs.KeyValueStore;
import org.gbif.kvs.hbase.HBaseKVStoreConfiguration;
import org.gbif.rest.client.configuration.ClientConfiguration;
import org.gbif.rest.client.geocode.GeocodeResponse;
import org.gbif.rest.client.geocode.GeocodeService;

import java.io.IOException;
import java.util.Collection;
import java.util.Objects;

import okhttp3.OkHttpClient;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.apache.hadoop.hbase.HBaseTestingUtility;
import org.apache.hadoop.hbase.client.HTable;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.util.Bytes;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.jackson.JacksonConverterFactory;

@Ignore
public class GeocodeKVStoreTestIT {

  private static HBaseTestingUtility utility;

  private static HTable geocodeKvTable;

  private static HBaseKVStoreConfiguration hBaseKVStoreConfiguration;

  private static KeyValueStore<LatLng,String> geocodeKeyValueStore;

  private static MockWebServer mockWebServer;


  private final static HBaseKVStoreConfiguration.Builder HBASE_KV_STORE_CONFIGURATION = HBaseKVStoreConfiguration.builder()
                                                                                  .withTableName("geocode_kv")
                                                                                  .withNumOfKeyBuckets(4)
                                                                                  .withColumnFamily("v");


  private static HBaseKVStoreConfiguration hBaseKVStoreConfiguration() {
    if (Objects.isNull(hBaseKVStoreConfiguration)) {
      hBaseKVStoreConfiguration = HBASE_KV_STORE_CONFIGURATION
                                    .withHBaseZk("localhost:" + utility.getZkCluster().getClientPort())
                                    .build();
    }
    return  hBaseKVStoreConfiguration;
  }

  private static HTable createTable() throws IOException {
    return utility.createTable(Bytes.toBytes(hBaseKVStoreConfiguration().getTableName()),
                               Bytes.toBytes(hBaseKVStoreConfiguration().getColumnFamily()));
  }

  private static KeyValueStore<LatLng,String> geocodeKeyValueStore() throws IOException {
    HBaseKVStoreConfiguration hBaseKVStoreConfiguration = hBaseKVStoreConfiguration();
    return GeocodeKVStoreFactory.simpleGeocodeKVStore(GeocodeKVStoreConfiguration.builder()
            .withJsonColumnQualifier("j") //stores JSON data
            .withCountryCodeColumnQualifier("c") //stores ISO country code
            .withHBaseKVStoreConfiguration(hBaseKVStoreConfiguration)
            .withGeocodeClientConfig(ClientConfiguration.builder()
                .withBaseApiUrl("https://api.gbif.org/v1/") //GBIF base API url
                .withFileCacheMaxSizeMb(64L) //Max file cache size
                .withTimeOut(60L) //Geocode service connection time-out
                .build())
            .build());
  }

  private static KeyValueStore<LatLng,String> geocodeKvStore;

  @Before
  public void setup() throws Exception {
    utility = new HBaseTestingUtility();
    utility.startMiniCluster();
    geocodeKvTable = createTable();
    geocodeKeyValueStore = geocodeKeyValueStore();
    mockWebServer = new MockWebServer();
  }

  @After
  public void tearDown() throws Exception {
    if (Objects.nonNull(geocodeKvTable)) {
      geocodeKvTable.close();
    }
    if (Objects.nonNull(utility)) {
      utility.shutdownMiniCluster();
    }
    if (Objects.nonNull(mockWebServer)) {
      mockWebServer.close();
    }
  }


  @Test
  public void insertTest() {
    MockResponse mockResponse = new MockResponse();
    mockResponse.setResponseCode(500);
    mockWebServer.enqueue(mockResponse);
    String countryCode = geocodeKeyValueStore.get(LatLng.builder().withLatitude(45.0).withLongitude(75.8).build());
    Assert.assertEquals("KZ", countryCode);
  }

  @Test
  public void responseTest() throws IOException {
    MockResponse mockResponse = new MockResponse();
    mockResponse.setResponseCode(500);
    mockWebServer.enqueue(mockResponse);
    OkHttpClient okHttpClient = new OkHttpClient.Builder().build();

    // Get an instance of Retrofit
    Retrofit retrofit = new Retrofit.Builder()
        .baseUrl("https://api.jsjsjsj.kkk")
        .addConverterFactory(JacksonConverterFactory.create())
        .client(okHttpClient)
        .build();

    // Get an instance of blogService
    GeocodeService geocodeService = retrofit.create(GeocodeService.class);
    Call<Collection<GeocodeResponse>> response = geocodeService.reverse(45.0, 55.9);
    Assert.assertFalse(response.execute().isSuccessful());
  }
}
