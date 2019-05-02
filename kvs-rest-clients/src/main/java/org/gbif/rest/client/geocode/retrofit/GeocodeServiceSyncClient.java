package org.gbif.rest.client.geocode.retrofit;

import org.gbif.rest.client.configuration.ClientConfiguration;
import org.gbif.rest.client.retrofit.RetrofitClientFactory;
import org.gbif.rest.client.geocode.Location;
import org.gbif.rest.client.geocode.GeocodeService;

import java.util.Collection;


import static org.gbif.rest.client.retrofit.SyncCall.syncCall;

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
     retrofitService = RetrofitClientFactory.createRetrofitClient(clientConfiguration,
                                                                  clientConfiguration.getBaseApiUrl(),
                                                                  GeocodeRetrofitService.class);
  }

  /**
   * Performs a synchronous call to the Geocode service.
   * @param latitude decimal latitude
   * @param longitude decimal longitude
   * @return the collection of proposed locations, an empty collection otherwise
   */
  @Override
  public Collection<Location> reverse(Double latitude, Double longitude) {
    return syncCall(retrofitService.reverse(latitude, longitude));
  }

}
