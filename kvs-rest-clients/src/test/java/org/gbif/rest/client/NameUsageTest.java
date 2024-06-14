package org.gbif.rest.client;

import org.gbif.rest.client.configuration.ClientConfiguration;
import org.gbif.rest.client.species.NameUsageMatchService;
import org.gbif.rest.client.species.NameUsageMatch;

public class NameUsageTest {

    public static void main(String[] args) {

        String baseApiUrl = "http://backbonebuild-vh.gbif.org:9101";

        ClientConfiguration clientConfiguration =
                ClientConfiguration.builder()
                        .withBaseApiUrl(baseApiUrl)
                        .withTimeOut(60L)
                        .withFileCacheMaxSizeMb(64L).build();

        NameUsageMatchService nameMatchService = ClientFactory.createNameMatchService(
                clientConfiguration
        );
        NameUsageMatch match = nameMatchService.match(null,
                "",
                "",
                "",
                "",
                "Acacia acuminata",
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
