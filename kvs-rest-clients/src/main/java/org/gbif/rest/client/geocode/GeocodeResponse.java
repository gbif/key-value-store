package org.gbif.rest.client.geocode;

import java.io.Serializable;
import java.util.Objects;
import java.util.StringJoiner;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Models the response of the {@link GeocodeService}.
 */
public class GeocodeResponse implements Serializable {

  private static final long serialVersionUID = -9137655613118727430L;

  private String id;
  private String type;
  private String source;

  @JsonProperty("title")
  private String countryName;

  private String isoCountryCode2Digit;

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

  public String getCountryName() {
    return countryName;
  }

  public void setCountryName(String countryName) {
    this.countryName = countryName;
  }

  public String getIsoCountryCode2Digit() {
    return isoCountryCode2Digit;
  }

  public void setIsoCountryCode2Digit(String isoCountryCode2Digit) {
    this.isoCountryCode2Digit = isoCountryCode2Digit;
  }


  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    GeocodeResponse that = (GeocodeResponse) o;
    return Objects.equals(id, that.id) &&
        Objects.equals(type, that.type) &&
        Objects.equals(source, that.source) &&
        Objects.equals(countryName, that.countryName) &&
        Objects.equals(isoCountryCode2Digit, that.isoCountryCode2Digit);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, type, source, countryName, isoCountryCode2Digit);
  }

  @Override
  public String toString() {
    return new StringJoiner(", ", GeocodeResponse.class.getSimpleName() + "[", "]")
        .add("id='" + id + "'")
        .add("type='" + type + "'")
        .add("source='" + source + "'")
        .add("countryName='" + countryName + "'")
        .add("isoCountryCode2Digit='" + isoCountryCode2Digit + "'")
        .toString();
  }
}
