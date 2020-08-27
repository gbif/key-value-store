package org.gbif.rest.client.geocode;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * Models the response content of the {@link GeocodeService}.
 */
@Data
public class Location implements Serializable {

  private static final long serialVersionUID = -9137655613118727430L;

  private String id;
  private String type;
  private String source;
  @JsonProperty("title")
  private String name;
  private String isoCountryCode2Digit;
  private Double distance;
}
