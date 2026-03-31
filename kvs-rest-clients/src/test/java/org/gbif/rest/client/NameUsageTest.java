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
package org.gbif.rest.client;

import org.gbif.kvs.species.NameUsageMatchRequest;
import org.gbif.rest.client.configuration.ClientConfiguration;
import org.gbif.rest.client.species.NameUsageMatchResponse;
import org.gbif.rest.client.species.NameUsageMatchingService;

import com.fasterxml.jackson.databind.ObjectMapper;

public class NameUsageTest {

    public static void main(String[] args) throws Exception {

//        String baseApiUrl = "http://localhost:8080";
        String baseApiUrl = "http://api.gbif.org";

        ClientConfiguration clientConfiguration =
                ClientConfiguration.builder()
                        .withBaseApiUrl(baseApiUrl)
                        .withTimeOutMillisec(60_000)
                        .withFileCacheMaxSizeMb(64L).build();

        NameUsageMatchingService nameMatchService = RestClientFactory.createNameMatchService(
                clientConfiguration
        );

//        NameUsageMatchRequest numr = NameUsageMatchRequest
//                .builder()
//                .withChecklistKey("checklistKey")
//                .withTaxonID("taxonID")
//                .withTaxonConceptID("taxonConceptID")
//                .withScientificNameID("scientificNameID")
//                .withUsageKey("usageKey")
//                .withKingdom("kingdom")
//                .withPhylum("phylum")
//                .withClazz("class")
//                .withOrder("order")
//                .withFamily("family")
//                .withGenus("genus")
//                .withSubgenus("subgenus")
//                .withSpecies("species")
//                .withScientificName("scientificName")
//                .withScientificNameAuthorship("scientificNameAuthorship")
//                .withTaxonRank("taxonRank")
//                .withVerbatimTaxonRank("verbatimRank")
//                .withGenericName("genericName")
//                .withSpecificEpithet("specificEpithet")
//                .withInfraspecificEpithet("infraspecificEpithet")
//                .withStrict(true)
//                .withVerbose(true)
//                .build();
//
//        NameUsageMatchResponse match = nameMatchService.match(numr);
//        System.out.println(match);


        NameUsageMatchRequest numr = NameUsageMatchRequest
                .builder()
                .withPhylum("Mollusca")
                .withClazz("Cephalopoda").build();

        System.out.println(new ObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(numr));

        NameUsageMatchResponse match = nameMatchService.match(numr);



        System.out.println(match);

        NameUsageMatchResponse match2 = nameMatchService.match(NameUsageMatchRequest
                .builder()
                .withScientificName("Pogona barbata").build());

        System.out.println(match2);

//        NameUsageMatchResponse match3 = nameMatchService.match(NameUsageMatchRequest
//                .builder()
//                .withChecklistKey("2d59e5db-57ad-41ff-97d6-11f5fb264527")
//                .withScientificName("Carcharodon carcharias").build());
//
//        System.out.println(match3);
    }
}
