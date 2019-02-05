package org.gbif.rest.client.geocode;

import java.util.Collection;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

/**
 * GBIF Geocode service client.
 */
public interface GeocodeService {

  /**
   * Builds an executable call to the reverse geocode service.
   * @param latitude decimal latitude
   * @param longitude decimal longitude
   * @return a executable call to the Geocode service
   */
  @GET("/v1/geocode/reverse")
  Call<Collection<GeocodeResponse>> reverse(@Query("lat") Double latitude, @Query("lng") Double longitude);
}
