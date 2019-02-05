package org.gbif.rest.client.geocode;

import org.gbif.rest.client.configuration.ClientConfiguration;
import org.gbif.rest.client.configuration.HttpClientFactory;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.jackson.JacksonConverterFactory;

/**
 * Factory of {@link GeocodeService} instances.
 */
public class GeocodeServiceFactory {

  /**
   * Hidden constructor.
   */
  private GeocodeServiceFactory() {
    //DO NOTHING
  }

  /**
   * Creates a new instance of the client {@link GeocodeService} using the provided client configuration.
   * @param config client configuration
   * @return a new GeocodeService instance
   */
  public static GeocodeService createGeocodeServiceClient(ClientConfiguration config) {
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
