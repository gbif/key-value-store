package org.gbif.rest.client.configuration;

import java.io.Serializable;
import java.util.Objects;

public class ClientConfiguration implements Serializable {

  private final String baseApiUrl;

  private final Long timeOut;

  private final Long fileCacheMaxSizeMb;

  private ClientConfiguration(String baseApiUrl, long timeOut, long fileCacheMaxSizeMb) {
    this.baseApiUrl = baseApiUrl;
    this.timeOut = timeOut;
    this.fileCacheMaxSizeMb = fileCacheMaxSizeMb;
  }

  public String getBaseApiUrl() {
    return baseApiUrl;
  }

  public Long getTimeOut() {
    return timeOut;
  }

  public Long getFileCacheMaxSizeMb() {
    return fileCacheMaxSizeMb;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ClientConfiguration that = (ClientConfiguration) o;
    return Objects.equals(timeOut, that.timeOut)
        && Objects.equals(fileCacheMaxSizeMb, that.fileCacheMaxSizeMb)
        && Objects.equals(baseApiUrl, that.baseApiUrl);
  }

  @Override
  public int hashCode() {
    return Objects.hash(baseApiUrl, timeOut, fileCacheMaxSizeMb);
  }

  /**
   * Creates a new {@link Builder} instance.
   * @return a new builder
   */
  public static Builder builder() {
    return new Builder();
  }

  public static class Builder {

    private String baseApiUrl;
    private Long timeOut = 60L;
    private Long fileCacheMaxSizeMb = 64L;

    /**
     * Hidden constructor to force use the containing class builder() method.
     */
    private Builder() {
      //DO NOTHING
    }


    public Builder withBaseApiUrl(String baseApiUrl) {
      this.baseApiUrl = baseApiUrl;
      return this;
    }

    public Builder withTimeOut(Long timeOut) {
      this.timeOut = timeOut;
      return this;
    }

    public Builder withFileCacheMaxSizeMb(Long fileCacheMaxSizeMb) {
      this.fileCacheMaxSizeMb = fileCacheMaxSizeMb;
      return this;
    }

    public ClientConfiguration build() {
      return new ClientConfiguration(baseApiUrl, timeOut, fileCacheMaxSizeMb);
    }
  }
}
