package org.gbif.rest.client.grscicoll.retrofit;

import org.gbif.api.model.collections.lookup.Match.MatchType;
import org.gbif.rest.client.configuration.ClientConfiguration;
import org.gbif.rest.client.grscicoll.LookupResponse;
import org.gbif.rest.client.grscicoll.LookupService;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

public class LookupServiceClientTest {

  @Ignore("manual test")
  @Test
  public void clientTest() {
    ClientConfiguration config =
        ClientConfiguration.builder().withBaseApiUrl("https://api.gbif-dev.org").build();
    LookupService lookupService = new LookupServiceSyncClient(config);
    LookupResponse response = lookupService.lookup("K", null, null, null, null, null, null);
    Assert.assertEquals(MatchType.FUZZY, response.getInstitutionMatch().getMatchType());
  }
}
