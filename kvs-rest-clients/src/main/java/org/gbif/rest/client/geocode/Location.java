package org.gbif.rest.client.geocode;

import java.io.Serializable;
import java.util.Objects;
import java.util.StringJoiner;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Models the response content of the {@link GeocodeService}.
 */
public class Location implements Serializable {

  private static final long serialVersionUID = -9137655613118727430L;

  private String id;
  private String type;
  private String source;
  @JsonProperty("title")
  private String name;
  private String isoCountryCode2Digit;
  private Double distance;

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

  public String getSource() {
    return source;
  }

  public void setSource(String source) {
    this.source = source;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getIsoCountryCode2Digit() {
    return isoCountryCode2Digit;
  }

  public void setIsoCountryCode2Digit(String isoCountryCode2Digit) {
    this.isoCountryCode2Digit = isoCountryCode2Digit;
  }

  public Double getDistance() {
    return distance;
  }

  public void setDistance(Double distance) {
    this.distance = distance;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    Location that = (Location) o;
    return Objects.equals(id, that.id) &&
        Objects.equals(type, that.type) &&
        Objects.equals(source, that.source) &&
        Objects.equals(name, that.name) &&
        Objects.equals(isoCountryCode2Digit, that.isoCountryCode2Digit) &&
        Objects.equals(distance, that.distance);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, type, source, name, isoCountryCode2Digit, distance);
  }

  @Override
  public String toString() {
    return new StringJoiner(", ", Location.class.getSimpleName() + "[", "]")
        .add("id='" + id + "'")
        .add("type='" + type + "'")
        .add("source='" + source + "'")
        .add("name='" + name + "'")
        .add("isoCountryCode2Digit='" + isoCountryCode2Digit + "'")
        .add("distance='" + distance + "'")
        .toString();
  }
}
