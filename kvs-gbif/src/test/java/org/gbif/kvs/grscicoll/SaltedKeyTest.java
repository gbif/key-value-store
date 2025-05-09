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
package org.gbif.kvs.grscicoll;

import org.gbif.api.vocabulary.Country;
import org.gbif.kvs.SaltedKeyGenerator;

import org.junit.Ignore;
import org.junit.Test;

public class SaltedKeyTest {

  @Test
  @Ignore("Manual test to get the key of a request")
  public void cacheKeyTest() {
    SaltedKeyGenerator keyGenerator = new SaltedKeyGenerator(10);

    GrscicollLookupRequest req = GrscicollLookupRequest.builder()
      .withInstitutionId("http://biocol.org/urn:lsid:biocol.org:col:15605")
      .withInstitutionCode("MeiseBG")
      .withCollectionCode("BR")
      .withCollectionId("gbif:ih:irn:124997")
      .withDatasetKey("b740eaa0-0679-41dc-acb7-990d562dfa37")
      .withCountry(Country.BELGIUM.getIso2LetterCode())
      .build();

    byte[] saltedKey = keyGenerator.computeKey(req.getLogicalKey());
    System.out.println(new String(saltedKey));
  }
}
