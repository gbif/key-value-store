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
import org.gbif.rest.client.geocode.GeocodeResponse;
import org.gbif.rest.client.geocode.GeocodeService;
import org.gbif.rest.client.grscicoll.GrscicollLookupService;
import org.gbif.rest.client.species.NameUsageMatchingService;

import org.apache.http.client.config.RequestConfig;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;

import feign.*;
import feign.httpclient.ApacheHttpClient;
import feign.jackson.JacksonDecoder;
import feign.jackson.JacksonEncoder;

/**
 * Factory class to create instances of the GBIF REST API clients.
 */
public class RestClientFactory {

    /**
     * Creates a new instance of the NameUsageMatchService using the provided clientConfiguration.
     * @param clientConfiguration the client configuration
     * @return the NameUsageMatchService
     */
    public static NameUsageMatchingService createNameMatchService(ClientConfiguration clientConfiguration) {
        return build(NameUsageMatchingService.class, clientConfiguration);
    }

    /**
     * Creates a new instance of the GeocodeService using the provided clientConfiguration.
     * @param clientConfiguration the client configuration
     * @return the GeocodeService
     */
    public static GeocodeService createGeocodeService(ClientConfiguration clientConfiguration) {
        return build(GeocodeService.class, clientConfiguration);
    }

    /**
     * Creates a new instance of the GrscicollLookupService using the provided clientConfiguration.
     * @param clientConfiguration the client configuration
     * @return the GrscicollLookupService
     */
    public static GrscicollLookupService createGrscicollLookupService(ClientConfiguration clientConfiguration) {
        return build(GrscicollLookupService.class, clientConfiguration);
    }

    /**
     * Creates a new client instance.
     */
    private static <T> T build(Class<T> clazz, ClientConfiguration clientConfiguration) {

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(
                new SimpleModule().addDeserializer(
                        GeocodeResponse.class,
                        new GeocodeResponse.GeocodeDeserializer()
                )
        );

        // Configure connection pool
        PoolingHttpClientConnectionManager connectionManager = new PoolingHttpClientConnectionManager();
        connectionManager.setMaxTotal(
                clientConfiguration.getMaxConnections() !=null ? clientConfiguration.getMaxConnections() : 5); // total pool size
        connectionManager.setDefaultMaxPerRoute(clientConfiguration.getMaxConnections() !=null ? clientConfiguration.getMaxConnections() : 5); // max per route

        // Configure timeouts
        RequestConfig requestConfig = RequestConfig.custom()
                .setConnectTimeout(
                        clientConfiguration.getConnectTimeoutMillisec() != null ?
                                clientConfiguration.getConnectTimeoutMillisec() :  60000) // 60 seconds
                .setSocketTimeout(clientConfiguration.getTimeOutMillisec() != null ?
                        clientConfiguration.getTimeOutMillisec() :  60000) // 60 seconds
                .build();

        // Build HTTP client
        CloseableHttpClient httpClient = HttpClientBuilder.create()
                .setConnectionManager(connectionManager)
                .setDefaultRequestConfig(requestConfig)
                .build();

        // Feign client with Apache HTTP client
        Client feignClient = new ApacheHttpClient(httpClient);

        return Feign.builder()
                .client(feignClient)
                .encoder(new JacksonEncoder())
                .decoder(new JacksonDecoder(objectMapper))
                .logger(new Logger.ErrorLogger())
                .logLevel(Logger.Level.BASIC)
                .target(clazz, clientConfiguration.getBaseApiUrl());
    }
}