/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.gbif.rest.client.geocode;

import java.io.IOException;
import java.io.Serializable;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Holds the list of location returned from the Geocode service.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class GeocodeResponse implements Serializable {

    @JsonProperty("locations")
    private List<Location> locations;

    public static class GeocodeDeserializer extends JsonDeserializer<GeocodeResponse> {

        @Override
        public GeocodeResponse deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException, JacksonException {
            List<GeocodeResponse.Location> locations = jsonParser
                    .readValueAs(new TypeReference<List<GeocodeResponse.Location>>(){});
            return new GeocodeResponse(locations);
        }
    }

    /**
     * Models the response content of the {@link GeocodeService}.
     */
    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Location implements Serializable {

      private static final long serialVersionUID = -9137655613118727430L;
      private String id;
      private String type;
      private String source;
      @JsonProperty("title")
      private String name;
      private String isoCountryCode2Digit;
      private Double distance;
      private Double distanceMeters;
    }
}
