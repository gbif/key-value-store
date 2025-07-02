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
package org.gbif.rest.client;

import org.gbif.kvs.grscicoll.GrscicollLookupRequest;
import org.gbif.rest.client.configuration.ClientConfiguration;
import org.gbif.rest.client.grscicoll.GrscicollLookupResponse;
import org.gbif.rest.client.grscicoll.GrscicollLookupService;

import org.junit.Assert;
import org.junit.Test;

public class GrscicollLookupTest {

  @Test
  public void clientTest() {
    ClientConfiguration config =
        ClientConfiguration.builder()
                .withTimeOutMillisec(60_000)
                .withFileCacheMaxSizeMb(64L)
                .withBaseApiUrl("https://api.gbif-uat.org/v1/")
                .build();

    GrscicollLookupService lookupService = RestClientFactory.createGrscicollLookupService(config);

    GrscicollLookupResponse response =
        lookupService.lookup(GrscicollLookupRequest.builder().withInstitutionCode("K").build());
    Assert.assertEquals(GrscicollLookupResponse.MatchType.FUZZY, response.getInstitutionMatch().getMatchType());
  }
}
