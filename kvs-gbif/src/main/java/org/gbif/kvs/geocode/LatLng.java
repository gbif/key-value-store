package org.gbif.kvs.geocode;

import org.gbif.kvs.hbase.Indexable;

import java.io.Serializable;
import java.util.Objects;
import java.util.StringJoiner;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

/** Geographic Coordinate: latitude and longitude. */
@Data
@Builder(setterPrefix = "with", builderClassName = "Builder")
@AllArgsConstructor
public class LatLng implements Serializable, Indexable {

  private Double latitude;
  private Double longitude;

  public LatLng() {}

  public void setLatitude(Double latitude) {
    this.latitude = latitude;
  }

  public void setLongitude(Double longitude) {
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
}
