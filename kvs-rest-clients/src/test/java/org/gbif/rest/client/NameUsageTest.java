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

import org.gbif.rest.client.configuration.ClientConfiguration;
import org.gbif.rest.client.species.NameUsageMatch;
import org.gbif.rest.client.species.NameUsageMatchingService;

public class NameUsageTest {

    public static void main(String[] args) {

        String baseApiUrl = "http://backbonebuild-vh.gbif.org:9101/";

        ClientConfiguration clientConfiguration =
                ClientConfiguration.builder()
                        .withBaseApiUrl(baseApiUrl)
                        .withTimeOut(60L)
                        .withFileCacheMaxSizeMb(64L).build();

        NameUsageMatchingService nameMatchService = RestClientFactory.createNameMatchService(
                clientConfiguration
        );
        NameUsageMatch match = nameMatchService.match(
                "",
                "",
                "",
                "",
                "Vanessa atalanta (Linnaeus,1758)",
                "",
                "",
                "",
                "",
                "",
                "",
                "",
                "",
                "",
                "",
                "",
                "",
                "",
                true,
                true);


        System.out.println(match);

    }
}
