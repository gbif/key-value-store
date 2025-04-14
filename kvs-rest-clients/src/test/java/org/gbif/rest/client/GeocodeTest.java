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

import org.gbif.kvs.geocode.GeocodeRequest;
import org.gbif.rest.client.configuration.ClientConfiguration;
import org.gbif.rest.client.geocode.*;

public class GeocodeTest {

    public static void main(String[] args) {

        String baseApiUrl = "https://api.gbif.org/v1/";

        ClientConfiguration clientConfiguration =
                ClientConfiguration.builder()
                        .withBaseApiUrl(baseApiUrl)
                        .withTimeOutMillisec(60_000)
                        .withFileCacheMaxSizeMb(64L).build();

        GeocodeService geocodeService = RestClientFactory.createGeocodeService(
                clientConfiguration
        );

        GeocodeResponse response = geocodeService.reverse(GeocodeRequest.builder().withLat(39.1)
                .withLng(147.2).withUncertaintyMeters(0.0).build());
        System.out.println(response);
    }
}
