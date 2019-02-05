package org.gbif.rest.client.geocode;

import org.gbif.rest.client.config.ClientConfig;
import org.gbif.rest.client.config.HttpClientFactory;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.jackson.JacksonConverterFactory;

public class GeocodeServiceFactory {

  public static GeocodeService createGeocodeServiceClient(ClientConfig config) {
    // create client
    OkHttpClient client = HttpClientFactory.createClient(config);

    // create service
    Retrofit retrofit =
        new Retrofit.Builder()
            .client(client)
            .baseUrl(config.getBaseApiUrl())
            .addConverterFactory(JacksonConverterFactory.create())
            .validateEagerly(true)
            .build();

    return retrofit.create(GeocodeService.class);
  }
}
