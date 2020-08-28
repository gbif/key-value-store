package org.gbif.rest.client.grscicoll;

import java.io.Closeable;
import java.util.UUID;

import org.gbif.api.vocabulary.Country;

/**
 * GrSciColl lookup service. This class is used for creation of Sync and Async clients. It is not
 * exposed outside this package.
 */
public interface GrscicollLookupService extends Closeable {

  GrscicollLookupResponse lookup(
      String institutionCode,
      String ownerInstitutionCode,
      String institutionId,
      String collectionCode,
      String collectionId,
      UUID datasetKey,
      Country country);
}
