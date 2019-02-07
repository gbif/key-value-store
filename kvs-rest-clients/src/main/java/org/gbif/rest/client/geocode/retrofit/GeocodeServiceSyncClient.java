package org.gbif.rest.client.geocode.retrofit;

import org.gbif.rest.client.configuration.ClientConfiguration;
import org.gbif.rest.client.geocode.GeocodeResponse;
import org.gbif.rest.client.geocode.GeocodeService;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.Objects;

import retrofit2.HttpException;
import retrofit2.Response;

/**
 * Represents an {@link GeocodeService} synchronous client.
 * It wraps a Retrofit client to perform the actual calls.
 */
public class GeocodeServiceSyncClient implements GeocodeService {

  //Retrofit internal client
  private final GeocodeRetrofitService retrofitService;

  /**
   * Creates an instance using the provided configuration settings.
   * @param clientConfiguration Rest client configuration
   */
  public GeocodeServiceSyncClient(ClientConfiguration clientConfiguration) {
     retrofitService = GeocodeServiceFactory.createGeocodeServiceClient(clientConfiguration);
  }

  /**
   * Performs a synchronous call to the Geocode service.
   * @param latitude decimal latitude
   * @param longitude decimal longitude
   * @return the collection of proposed locations, an empty collection otherwise
   */
  @Override
  public Collection<GeocodeResponse> reverse(Double latitude, Double longitude) {
    try {
      Response<Collection<GeocodeResponse>> response = retrofitService.reverse(latitude, longitude).execute();
      if (response.isSuccessful()) {
        return Objects.nonNull(response.body()) && !response.body().isEmpty()? response.body() : Collections.emptyList();
      }
      throw new HttpException(response); // Propagates the failed response
    } catch (IOException ex) {
      throw new RuntimeException(ex);
    }

  }
}
