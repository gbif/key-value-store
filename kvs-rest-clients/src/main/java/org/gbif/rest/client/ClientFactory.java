package org.gbif.rest.client;

import org.gbif.rest.client.configuration.ClientConfiguration;
import org.gbif.rest.client.configuration.ServiceConfig;
import org.gbif.rest.client.geocode.GeocodeService;
import org.gbif.rest.client.grscicoll.GrscicollLookupService;
import org.gbif.rest.client.species.NameUsageMatchService;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.core.env.PropertySource;

/**
 * Factory class to create instances of the GBIF REST API clients.
 */
public class ClientFactory {

    /**
     * Creates a new instance of the NameUsageMatchService using the provided clientConfiguration.
     * @param clientConfiguration
     * @return
     */
    public static NameUsageMatchService createNameMatchService(ClientConfiguration clientConfiguration) {
        AnnotationConfigApplicationContext context =
                new AnnotationConfigApplicationContext(ServiceConfig.class);
        context.getEnvironment().getPropertySources().addLast(
                new ClientConfigurationSource(clientConfiguration)
        );
        return context.getBean(NameUsageMatchService.class);
    }

    /**
     * Creates a new instance of the GeocodeService using the provided clientConfiguration.
     * @param clientConfiguration
     * @return
     */
    public static GeocodeService createGeocodeService(ClientConfiguration clientConfiguration) {
        AnnotationConfigApplicationContext context =
                new AnnotationConfigApplicationContext(ServiceConfig.class);
        context.getEnvironment().getPropertySources().addLast(
                new ClientConfigurationSource(clientConfiguration)
        );
        return context.getBean(GeocodeService.class);
    }

    /**
     * Creates a new instance of the GrscicollLookupService using the provided clientConfiguration.
     * @param clientConfiguration
     * @return
     */
    public static GrscicollLookupService createGrscicollLookupService(ClientConfiguration clientConfiguration) {
        AnnotationConfigApplicationContext context =
                new AnnotationConfigApplicationContext(ServiceConfig.class);
        context.getEnvironment().getPropertySources().addLast(
                new ClientConfigurationSource(clientConfiguration)
        );
        return context.getBean(GrscicollLookupService.class);
    }


    static class ClientConfigurationSource extends PropertySource<String> {

        private final ClientConfiguration clientConfiguration;

        public ClientConfigurationSource(ClientConfiguration clientConfiguration) {
            super("custom");
            this.clientConfiguration = clientConfiguration;
        }

        @Override
        public String getProperty(String name) {
            if (name.endsWith(".baseApiUrl")) {
                return clientConfiguration.getBaseApiUrl();
            }
            if (name.endsWith(".timeOut")) {
                return clientConfiguration.getTimeOut() != null ? clientConfiguration.getTimeOut().toString() : null;
            }
            if (name.endsWith(".fileCacheMaxSizeMb")) {
                return clientConfiguration.getFileCacheMaxSizeMb() != null ? clientConfiguration.getFileCacheMaxSizeMb().toString() : null;
            }
            return null;
        }
    }
}


