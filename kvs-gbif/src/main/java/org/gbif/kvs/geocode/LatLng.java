package org.gbif.kvs.geocode;

import org.gbif.kvs.hbase.Indexable;

import java.io.Serializable;
import java.util.Objects;
import java.util.StringJoiner;

/** Geographic Coordinate: latitude and longitude. */
public class LatLng implements Serializable, Indexable {

  private final Double latitude;
  private final Double longitude;

  /**
   * Full constructor.
   *
   * @param latitude decimal latitude
   * @param longitude decimal longitude
   */
  public LatLng(Double latitude, Double longitude) {
    this.latitude = latitude;
    this.longitude = longitude;
  }

  /**
   * Facrtory method.
   * @param latitude decimal latitude
   * @param longitude decimal longitude
   * @return a new instance of LatLng
   */
  public static LatLng create(Double latitude, Double longitude) {
    return new LatLng(latitude,longitude);
  }

  /** @return decimal latitude */
  public Double getLatitude() {
    return latitude;
  }

  /** @return decimal longitude */
  public Double getLongitude() {
    return longitude;
  }

  /**
   * Is this coordinates valid?. Both can't be null and -90 <= latitude <= 90 and -180 <= longitude
   * <= 180.
   *
   * @return true if the coordinate is valid, false otherwise
   */
  public boolean isValid() {
    return Objects.nonNull(latitude)
        && Objects.nonNull(longitude)
        && latitude <= 90.0
        && latitude >= -90
        && longitude <= 180
        && longitude >= -180;
  }

  /**
   * Concatenates as a string the latitude and longitude.
   *
   * @return latitude + longitude
   */
  @Override
  public String getLogicalKey() {
    return latitude.toString() + '|' + longitude.toString();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    LatLng latLng = (LatLng) o;
    return Objects.equals(latitude, latLng.latitude) && Objects.equals(longitude, latLng.longitude);
  }

  @Override
  public int hashCode() {
    return Objects.hash(latitude, longitude);
  }

  @Override
  public String toString() {
    return new StringJoiner(", ", LatLng.class.getSimpleName() + "[", "]")
        .add("latitude=" + latitude)
        .add("longitude=" + longitude)
        .toString();
  }

  /**
   * Creates a new {@link Builder} instance.
   * @return a new builder
   */
  public static Builder builder() {
    return new Builder();
  }

  /** LatLng builder utility. */
  public static class Builder {
    private Double latitude;
    private Double longitude;

    /**
     * Hidden constructor to force use the containing class builder() method.
     */
    private Builder() {
      //DO NOTHING
    }

    public Builder withLatitude(Double latitude) {
      this.latitude = latitude;
      return this;
    }

    public Builder withLongitude(Double longitude) {
      this.longitude = longitude;
      return this;
    }

    public LatLng build() {
      return new LatLng(latitude, longitude);
    }
  }
}
