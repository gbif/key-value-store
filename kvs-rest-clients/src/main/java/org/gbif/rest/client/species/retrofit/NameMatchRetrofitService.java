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
package org.gbif.rest.client.species.retrofit;

import org.gbif.rest.client.species.ChecklistbankService;
import org.gbif.rest.client.species.NameUsageMatch;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

/**
 * see {@link ChecklistbankService}
 */
public interface NameMatchRetrofitService {

  /**
   * See {@link ChecklistbankService#match(String, String, String, String, String, String, String, String, boolean, boolean)}
   */
  @GET("species/match2")
  Call<NameUsageMatch> match(
    @Query("kingdom") String kingdom,
    @Query("phylum") String phylum,
    @Query("class") String clazz,
    @Query("order") String order,
    @Query("family") String family,
    @Query("genus") String genus,
    @Query("scientificName") String scientificName,
    @Query("genericName") String genericName,
    @Query("specificEpithet") String specificEpithet,
    @Query("infraspecificEpithet") String infraspecificEpithet,
    @Query("scientificNameAuthorship") String scientificNameAuthorship,
    @Query("rank") String rank,
    @Query("verbose") boolean verbose,
    @Query("strict") boolean strict
  );

}
