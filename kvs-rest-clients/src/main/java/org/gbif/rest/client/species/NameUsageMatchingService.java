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
package org.gbif.rest.client.species;
import org.gbif.kvs.species.NameUsageMatchRequest;

import feign.Headers;
import feign.Param;
import feign.QueryMap;
import feign.RequestLine;

/**
 * GBIF Backbone name match and IUCN RedList services.
 */
public interface NameUsageMatchingService {

  @RequestLine("GET /v2/species/match")  // Adjust the endpoint as needed
  @Headers({
          "Content-Type: application/json",
          "Accept: application/json"
  })
  NameUsageMatchResponse match(@QueryMap NameUsageMatchRequest identification);

  @RequestLine("GET /v2/species/match/metadata")  // Adjust the endpoint as needed
  @Headers({
          "Content-Type: application/json",
          "Accept: application/json"
  })
  Metadata getMetadata(@Param(value = "checklistKey") String checklistKey);
}
