/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.gbif.rest.client.geocode.retrofit;

import org.gbif.rest.client.geocode.Location;

import java.util.List;

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
   * @param uncertaintyMeters coordinate uncertainty in meters
   * @return a executable call to the Geocode service
   */
  @GET("geocode/reverse")
  Call<List<Location>> reverse(@Query("lat") Double latitude, @Query("lng") Double longitude, @Query("uncertaintyMeters") Double uncertaintyMeters);
}
