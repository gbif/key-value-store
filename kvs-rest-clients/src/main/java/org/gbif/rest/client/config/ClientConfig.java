package org.gbif.rest.client.config;

import java.io.Serializable;
import java.util.Objects;

public class ClientConfig implements Serializable {

    private final String baseApiUrl;

    private final Long timeOut;

    private final Long fileCacheMaxSizeMb;

    private ClientConfig(String baseApiUrl, long timeOut, long fileCacheMaxSizeMb) {
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
        if (this == o)   {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ClientConfig that = (ClientConfig) o;
        return Objects.equals(timeOut,that.timeOut) &&
                Objects.equals(fileCacheMaxSizeMb,that.fileCacheMaxSizeMb) &&
                Objects.equals(baseApiUrl, that.baseApiUrl);
    }

    @Override
    public int hashCode() {
        return Objects.hash(baseApiUrl, timeOut, fileCacheMaxSizeMb);
    }

    public static class Builder {
        private String baseApiUrl;
        private Long timeOut = 60L;
        private Long fileCacheMaxSizeMb = 64L;

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

        public ClientConfig build() {
            return new ClientConfig(baseApiUrl, timeOut, fileCacheMaxSizeMb);
        }
    }
}
