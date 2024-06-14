package org.gbif.rest.client;

import org.gbif.rest.client.configuration.ClientConfiguration;
import org.gbif.rest.client.geocode.GeocodeResponse;
import org.gbif.rest.client.geocode.GeocodeService;
import org.gbif.rest.client.species.NameUsageMatch;
import org.gbif.rest.client.species.NameUsageMatchService;

import java.util.List;

public class GeocodeTest {
    public static void main(String[] args) {

        String baseApiUrl = "https://api.gbif.org/v1/";

        ClientConfiguration clientConfiguration =
                ClientConfiguration.builder()
                        .withBaseApiUrl(baseApiUrl)
                        .withTimeOut(60L)
                        .withFileCacheMaxSizeMb(64L).build();

        GeocodeService geocodeService = ClientFactory.createGeocodeService(
                clientConfiguration
        );
        GeocodeResponse response = geocodeService.reverse(39.1, 147.2, 0.0);
        System.out.println(response);
    }
}
