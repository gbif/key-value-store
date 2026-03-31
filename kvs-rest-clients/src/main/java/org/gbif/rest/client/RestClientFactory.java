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

import feign.Contract;
import feign.form.FormEncoder;
import org.gbif.rest.client.configuration.ClientConfiguration;
import org.gbif.rest.client.geocode.GeocodeResponse;
import org.gbif.rest.client.geocode.GeocodeService;
import org.gbif.rest.client.grscicoll.GrscicollLookupService;
import org.gbif.rest.client.species.NameUsageMatchingService;

import java.nio.charset.StandardCharsets;

import org.apache.http.client.config.RequestConfig;
import org.apache.http.config.ConnectionConfig;
import org.apache.http.config.SocketConfig;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;

import feign.Feign;
import feign.httpclient.ApacheHttpClient;
import feign.jackson.JacksonDecoder;

/**
 * Factory class to create instances of the GBIF REST API clients.
 * This is a simplified way of creating clients largely for test purposes.
 * Clients can also be created using the ClientBuilder in the gbif-common-ws module
 * which provides more configuration options.
 */
public class RestClientFactory {

    private static final Integer DEFAULT_CONNECT_TIMEOUT_MILLISECONDS = 10_000;
    private static final Integer DEFAULT_READ_TIMEOUT_MILLISECONDS = 60_000;

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

        Feign.Builder builder =
                Feign.builder()
                        .client(new ApacheHttpClient(newMultithreadedClient(
                                clientConfiguration.getMaxConnections() != null
                                        ? clientConfiguration.getMaxConnections()
                                        : 3,
                                clientConfiguration.getMaxConnections() != null
                                        ? clientConfiguration.getMaxConnections()
                                        : 3,
                                clientConfiguration.getConnectTimeoutMillisec() != null
                                        ? clientConfiguration.getConnectTimeoutMillisec()
                                        : DEFAULT_CONNECT_TIMEOUT_MILLISECONDS,
                                clientConfiguration.getTimeOutMillisec() != null
                                        ? clientConfiguration.getTimeOutMillisec()
                                        : DEFAULT_READ_TIMEOUT_MILLISECONDS,
                                clientConfiguration.getTimeOutMillisec() != null
                                        ? clientConfiguration.getTimeOutMillisec()
                                        : DEFAULT_READ_TIMEOUT_MILLISECONDS
                        )))
                        .encoder(new FormEncoder())
                        .decoder(new JacksonDecoder(objectMapper))
                        .contract(new Contract.Default())
                        .decode404();
        return builder.target(clazz, clientConfiguration.getBaseApiUrl());
    }

    /**
     * Creates a Http multithreaded client.
     */
    static CloseableHttpClient newMultithreadedClient(Integer maxConnections,
                                                      Integer maxPerRoute,
                                                      Integer socketTimeout,
                                                      Integer connectionTimeout,
                                                      Integer connectionRequestTimeout) {
        return HttpClients.custom()
                .setMaxConnTotal(maxConnections)
                .setMaxConnPerRoute(maxPerRoute)
                .setDefaultSocketConfig(
                        SocketConfig.custom().setSoTimeout(socketTimeout).build())
                .setDefaultConnectionConfig(
                        ConnectionConfig.custom()
                                .setCharset(StandardCharsets.UTF_8)
                                .build())
                .setDefaultRequestConfig(
                        RequestConfig.custom()
                                .setConnectTimeout(connectionTimeout)
                                .setConnectionRequestTimeout(connectionRequestTimeout)
                                .build())
                .build();
    }
}
