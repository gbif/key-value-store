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

    GrscicollLookupRequest req = new GrscicollLookupRequest();
    req.setInstitutionId("http://biocol.org/urn:lsid:biocol.org:col:15605");
    req.setInstitutionCode("MeiseBG");
    req.setCollectionCode("BR");
    req.setCollectionId("gbif:ih:irn:124997");
    req.setDatasetKey("b740eaa0-0679-41dc-acb7-990d562dfa37");
    //    req.setOwnerInstitutionCode("NULL");
    req.setCountry(Country.BELGIUM.getIso2LetterCode());

    byte[] saltedKey = keyGenerator.computeKey(req.getLogicalKey());
    System.out.println(new String(saltedKey));
  }
}
