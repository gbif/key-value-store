package org.gbif.rest.client.grscicoll.retrofit;

import org.gbif.api.model.collections.lookup.Match.MatchType;
import org.gbif.rest.client.configuration.ClientConfiguration;
import org.gbif.rest.client.grscicoll.GrscicollLookupResponse;
import org.gbif.rest.client.grscicoll.GrscicollLookupService;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

public class GrscicollLookupServiceClientTest {

  @Ignore("manual test")
  @Test
  public void clientTest() {
    ClientConfiguration config =
        ClientConfiguration.builder().withBaseApiUrl("https://api.gbif-dev.org").build();
    GrscicollLookupService lookupService = new GrscicollLookupServiceSyncClient(config);
    GrscicollLookupResponse response =
        lookupService.lookup("K", null, null, null, null, null, null);
    Assert.assertEquals(MatchType.FUZZY, response.getInstitutionMatch().getMatchType());
  }
}
