# kvs-rest-clients

This module contains GBIF REST clients to access GBIF API resources.
All clients are implemented using [Feign](https://docs.spring.io/spring-cloud-openfeign/docs/current/reference/html/) and [Jackson](https://github.com/FasterXML/jackson-dataformat-xml).
This is a java 11 library.

To create an instance of a Rest client, a [ClientConfiguration](src/main/java/org/gbif/rest/client/configuration/ClientConfiguration.java) is required, for example:

```
ClientConfiguration clientConfiguration = ClientConfiguration.builder()
                                            .withBaseApiUrl("http://api.gbif.org/v1")
                                            .withTimeOut(60000L)
                                            .withFileCacheMaxSizeMb(128L)
                                            .build();

GeocodeService geocodeService = RestClientFactory.createGeocodeService(clientConfiguration);
```

Clients can also be created using the [gbif-common-ws](https://github.com/gbif/gbif-common-ws) module, for example:

``` 
NameUsageMatchingService nameUsageMatchingService = new org.gbif.ws.client.ClientBuilder()
        .withUrl("http://api.gbif.org/v1")
        .withObjectMapper(JacksonJsonObjectMapperProvider.getObjectMapperWithBuilderSupport())
        .withFormEncoder()
        .build(NameUsageMatchingService.class);
```


## Build

To build, install and run tests, execute the Maven command:

`mvn clean package install -U`