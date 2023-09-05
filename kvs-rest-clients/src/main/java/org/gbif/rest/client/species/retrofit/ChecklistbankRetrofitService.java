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
import org.gbif.rest.client.species.IucnRedListCategory;
import org.gbif.rest.client.species.NameUsageSearchResponse;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

/**
 * see {@link ChecklistbankService}
 */
public interface ChecklistbankRetrofitService {

  /**
   * See {@link org.gbif.rest.client.species.ChecklistbankService#getIucnRedListCategory(Integer)}
   */
  @GET("species/{nubKey}/iucnRedListCategory")
  Call<IucnRedListCategory>  getIucnRedListCategory(@Path("nubKey") Integer nubKey);

  /**
   * See {@link org.gbif.rest.client.species.ChecklistbankService#lookupNameUsage(String, String)}
   */
  @GET("species")
  Call<NameUsageSearchResponse> lookupNameUsage(@Query("datasetKey") String datasetKey, @Query("sourceId") String sourceId);

}
