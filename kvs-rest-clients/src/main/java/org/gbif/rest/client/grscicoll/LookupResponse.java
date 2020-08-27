package org.gbif.rest.client.grscicoll;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.gbif.api.model.collections.Address;
import org.gbif.api.model.collections.lookup.Match.MatchType;
import org.gbif.api.model.collections.lookup.Match.Reason;
import org.gbif.api.model.registry.Identifier;

import lombok.Data;

import static org.gbif.api.model.collections.lookup.Match.Status;

@Data
public class LookupResponse {

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
    private String code;
    private String name;
    private Map<String, String> alternativeCodes = new HashMap();
    private List<Identifier> identifiers = new ArrayList();
    private Address mailingAddress;
    private Address address;
  }

  @Data
  public static class CollectionResponse implements Serializable {
    private UUID key;
    private String code;
    private String name;
    private UUID institutionKey;
    private Map<String, String> alternativeCodes = new HashMap();
    private List<Identifier> identifiers = new ArrayList();
    private Address mailingAddress;
    private Address address;
  }
}
