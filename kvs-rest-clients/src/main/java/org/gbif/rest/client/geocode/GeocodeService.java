package org.gbif.rest.client.geocode;

import java.util.Collection;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface GeocodeService {

  @GET("/v1/geocode/reverse")
  Call<Collection<GeocodeResponse>> reverse(
      @Query("lat") Double latitude, @Query("lng") Double longitude);
}
