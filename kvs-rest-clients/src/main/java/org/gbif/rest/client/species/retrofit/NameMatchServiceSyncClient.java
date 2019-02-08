package org.gbif.rest.client.species.retrofit;

import org.gbif.rest.client.species.NameMatchService;
import org.gbif.rest.client.species.NameUsageMatch;

import java.util.Map;

import static org.gbif.rest.client.retrofit.SyncCall.syncCall;

/**
 * Synchronous Retrofit service client for the NameMatch GBIF service.
 */
public class NameMatchServiceSyncClient implements NameMatchService {

  //Wrapped service
  private final NameMatchRetrofitService nameMatchRetrofitService;

  /**
   * Creates synchronous client that wraps the Retrofit client.
   * @param nameMatchRetrofitService retrofit service instance
   */
  public NameMatchServiceSyncClient(NameMatchRetrofitService nameMatchRetrofitService) {
    this.nameMatchRetrofitService = nameMatchRetrofitService;
  }

  /**
   * See {@link NameMatchService#match(String, String, String, String, String, String, String, String, boolean, boolean)}
   */
  @Override
  public NameUsageMatch match(String kingdom, String phylum, String clazz, String order, String family, String genus,
                              String rank, String name, boolean verbose, boolean strict) {
    return syncCall(nameMatchRetrofitService.match(kingdom, phylum, clazz, order, family, genus, rank, name, verbose,
                                                   strict));
  }

  /**
   * See {@link NameMatchService#match(Map)}
   */
  @Override
  public NameUsageMatch match(Map<String, String> params) {
    return syncCall(nameMatchRetrofitService.match(params));
  }
}
