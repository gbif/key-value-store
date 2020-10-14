package org.gbif.rest.client.grscicoll;

import java.io.Serializable;
import java.util.Set;
import java.util.UUID;

import org.gbif.api.model.collections.lookup.Match.MatchType;
import org.gbif.api.model.collections.lookup.Match.Reason;

import lombok.Data;

import static org.gbif.api.model.collections.lookup.Match.Status;

@Data
public class GrscicollLookupResponse {

  private Match<InstitutionResponse> institutionMatch;
  private Match<CollectionResponse> collectionMatch;

  @Data
  public static class Match<T> implements Serializable {
    private MatchType matchType;
    private Status status;
    private Set<Reason> reasons;
    private T entityMatched;
  }

  @Data
  public static class InstitutionResponse implements Serializable {
    private UUID key;
  }

  @Data
  public static class CollectionResponse implements Serializable {
    private UUID key;
  }
}
