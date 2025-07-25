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

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.cloud.openfeign.SpringQueryMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * GBIF Backbone name match and IUCN RedList services.
 */
@FeignClient(name = "nameUsageService", url = "${nameUsageService.baseApiUrl}")
public interface NameUsageMatchingService {

  @RequestMapping(method = RequestMethod.GET, value = "/v2/species/match", consumes = "application/json")
  NameUsageMatchResponse match(@SpringQueryMap NameUsageMatchRequest identification);

  @RequestMapping(method = RequestMethod.GET, value = "/v2/species/match/metadata", consumes = "application/json")
  Metadata getMetadata(@RequestParam(value = "checklistKey", required = false) String checklistKey);
}
