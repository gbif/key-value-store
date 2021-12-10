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
package org.gbif.rest.client.grscicoll.retrofit;

import org.gbif.api.vocabulary.Country;
import org.gbif.rest.client.grscicoll.GrscicollLookupResponse;

import java.util.UUID;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

/**
 * GrSciColl Retrofit Service client. This class is used for creation of Sync and Async clients. It
 * is not exposed outside this package.
 */
interface GrscicollLookupRetrofitService {

  @GET("grscicoll/lookup")
  Call<GrscicollLookupResponse> lookup(
      @Query("institutionCode") String institutionCode,
      @Query("ownerInstitutionCode") String ownerInstitutionCode,
      @Query("institutionId") String institutionId,
      @Query("collectionCode") String collectionCode,
      @Query("collectionId") String collectionId,
      @Query("datasetKey") UUID datasetKey,
      @Query("country") Country country,
      @Query("verbose") boolean verbose);
}
