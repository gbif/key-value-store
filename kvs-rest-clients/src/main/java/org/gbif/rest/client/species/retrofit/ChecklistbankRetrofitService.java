package org.gbif.rest.client.species.retrofit;

import org.gbif.rest.client.species.ChecklistbankService;
import org.gbif.rest.client.species.IucnRedListCategory;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

/**
 * see {@link ChecklistbankService}
 */
public interface ChecklistbankRetrofitService {

  /**
   * See {@link org.gbif.rest.client.species.ChecklistbankService#getIucnRedListCategory(Integer)}
   */
  @GET("species/{nubKey}/iucnRedListCategory")
  Call<IucnRedListCategory>  getIucnRedListCategory(@Path("nubKey") Integer nubKey);
}
