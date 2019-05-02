package org.gbif.rest.client.geocode.retrofit;

import org.gbif.rest.client.geocode.Location;

import java.util.Collection;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

/**
 * GBIF Geocode Retrofit Service client.
 * This class is used for creation of Sync and Async clients. It is not exposed outside this package.
 */
interface GeocodeRetrofitService {

  /**
   * Builds an executable call to the reverse geocode service.
   * @param latitude decimal latitude
   * @param longitude decimal longitude
   * @return a executable call to the Geocode service
   */
  @GET("/v1/geocode/reverse")
  Call<Collection<Location>> reverse(@Query("lat") Double latitude, @Query("lng") Double longitude);
}
