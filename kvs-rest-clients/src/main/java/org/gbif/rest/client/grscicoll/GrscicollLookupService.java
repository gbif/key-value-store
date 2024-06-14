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
package org.gbif.rest.client.grscicoll;

import org.gbif.api.vocabulary.Country;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;


/**
 * GrSciColl lookup service
 */
@FeignClient(name = "grscicoll", url = "${grscicoll.baseApiUrl}")
public interface GrscicollLookupService {

  @RequestMapping(method = RequestMethod.GET, value = "grscicoll/lookup")
  GrscicollLookupResponse lookup(
          @RequestParam("institutionCode") String institutionCode,
          @RequestParam("ownerInstitutionCode") String ownerInstitutionCode,
          @RequestParam("institutionId") String institutionId,
          @RequestParam("collectionCode") String collectionCode,
          @RequestParam("collectionId") String collectionId,
          @RequestParam("datasetKey") String datasetKey,
          @RequestParam("country") Country country,
          @RequestParam("verbose") boolean verbose);
}
