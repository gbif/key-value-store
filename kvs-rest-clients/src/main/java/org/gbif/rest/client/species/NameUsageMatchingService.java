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
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * GBIF Backbone name match and IUCN RedList services.
 */
@FeignClient(name = "nameUsageService", url = "${nameUsageService.baseApiUrl}")
public interface NameUsageMatchingService {

  @RequestMapping(method = RequestMethod.GET, value = "/v2/species/match", consumes = "application/json")
  NameUsageMatch match(
      @RequestParam(value = "usageKey", required = false) String usageKey,
      @RequestParam(value = "taxonID", required = false) String taxonID,
      @RequestParam(value = "taxonConceptID", required = false) String taxonConceptID,
      @RequestParam(value = "scientificNameID", required = false) String scientificNameID,
      @RequestParam(value = "scientificName", required = false) String scientificName,
      @RequestParam(value = "scientificNameAuthorship", required = false) String authorship,
      @RequestParam(value = "taxonRank", required = false) String rank,
      @RequestParam(value = "genericName", required = false) String genericName,
      @RequestParam(value = "specificEpithet", required = false) String specificEpithet,
      @RequestParam(value = "infraspecificEpithet", required = false) String infraspecificEpithet,
      @RequestParam(value = "kingdom", required = false) String kingdom,
      @RequestParam(value = "phylum", required = false) String phylum,
      @RequestParam(value = "class", required = false) String clazz,
      @RequestParam(value = "order", required = false) String order,
      @RequestParam(value = "family", required = false) String family,
      @RequestParam(value = "genus", required = false) String genus,
      @RequestParam(value = "subgenus", required = false) String subgenus,
      @RequestParam(value = "species", required = false) String species,
      @RequestParam(value = "strict", required = false) Boolean strict,
      @RequestParam(value = "verbose", required = false) Boolean verbose
  );
}
