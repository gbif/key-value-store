package org.gbif.rest.client.grscicoll.retrofit;

import java.util.UUID;

import org.gbif.api.vocabulary.Country;
import org.gbif.rest.client.grscicoll.GrscicollLookupResponse;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

/**
 * GrSciColl Retrofit Service client. This class is used for creation of Sync and Async clients. It
 * is not exposed outside this package.
 */
interface GrscicollLookupRetrofitService {

  @GET("/v1/grscicoll/lookup")
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
