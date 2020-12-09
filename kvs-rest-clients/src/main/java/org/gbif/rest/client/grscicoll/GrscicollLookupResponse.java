package org.gbif.rest.client.grscicoll;

import java.io.Serializable;
import java.util.UUID;

import org.gbif.api.model.collections.lookup.Match.MatchType;

import lombok.Data;

import static org.gbif.api.model.collections.lookup.Match.Status;

@Data
public class GrscicollLookupResponse {

  private Match institutionMatch;
  private Match collectionMatch;

  @Data
  public static class Match implements Serializable {
    private MatchType matchType;
    private Status status;
    private EntityMatchedResponse entityMatched;
  }

  @Data
  public static class EntityMatchedResponse implements Serializable {
    private UUID key;
  }

}
