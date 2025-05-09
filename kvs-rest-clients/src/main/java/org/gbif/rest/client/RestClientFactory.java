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

import feign.httpclient.ApacheHttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.config.ConnectionConfig;
import org.apache.http.config.SocketConfig;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.gbif.rest.client.configuration.ClientConfiguration;
import org.gbif.rest.client.geocode.GeocodeResponse;
import org.gbif.rest.client.geocode.GeocodeService;
import org.gbif.rest.client.grscicoll.GrscicollLookupService;
import org.gbif.rest.client.species.NameUsageMatchingService;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.*;

import org.springframework.cloud.openfeign.AnnotatedParameterProcessor;
import org.springframework.cloud.openfeign.annotation.PathVariableParameterProcessor;
import org.springframework.cloud.openfeign.annotation.QueryMapParameterProcessor;
import org.springframework.cloud.openfeign.annotation.RequestHeaderParameterProcessor;
import org.springframework.cloud.openfeign.annotation.RequestParamParameterProcessor;
import org.springframework.cloud.openfeign.support.SpringMvcContract;
import org.springframework.web.bind.annotation.RequestMapping;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;

import feign.Feign;
import feign.MethodMetadata;
import feign.Util;
import feign.form.spring.SpringFormEncoder;
import feign.jackson.JacksonDecoder;

import static feign.Util.checkState;
import static feign.Util.emptyToNull;
import static org.springframework.core.annotation.AnnotatedElementUtils.findMergedAnnotation;

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
                        .encoder(new SpringFormEncoder())
                        .decoder(new JacksonDecoder(objectMapper))
                        .contract(ClientContract.withDefaultProcessors())
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
                                .setCharset(Charset.forName(StandardCharsets.UTF_8.name()))
                                .build())
                .setDefaultRequestConfig(
                        RequestConfig.custom()
                                .setConnectTimeout(connectionTimeout)
                                .setConnectionRequestTimeout(connectionRequestTimeout)
                                .build())
                .build();
    }

    static class ClientContract extends SpringMvcContract {

        private ClientContract(List<AnnotatedParameterProcessor> annotatedParameterProcessors) {
            super(annotatedParameterProcessors);
        }

        public static ClientContract withDefaultProcessors() {
            return new ClientContract(
                    Arrays.asList(
                            new PathVariableParameterProcessor(),
                            new RequestParamParameterProcessor(),
                            new RequestHeaderParameterProcessor(),
                            new QueryMapParameterProcessor()));
        }

        @Override
        public List<MethodMetadata> parseAndValidateMetadata(final Class<?> targetType) {
            checkState(
                    targetType.getTypeParameters().length == 0,
                    "Parameterized types unsupported: %s",
                    targetType.getSimpleName());
            final Map<String, MethodMetadata> result = new LinkedHashMap<>();

            for (final Method method : targetType.getMethods()) {
                if (method.getDeclaringClass() == Object.class
                        || (method.getModifiers() & Modifier.STATIC) != 0
                        || Util.isDefault(method)
                        // skip default methods which related to generic inheritance
                        // also default methods are considered as "unsupported operations"
                        || method.toString().startsWith("public default")
                        // skip not annotated methods (consider as "not implemented")
                        || method.getAnnotations().length == 0) {
                    continue;
                }
                final MethodMetadata metadata = this.parseAndValidateMetadata(targetType, method);
                checkState(
                        !result.containsKey(metadata.configKey()),
                        "Overrides unsupported: %s",
                        metadata.configKey());
                result.put(metadata.configKey(), metadata);
            }

            return new ArrayList<>(result.values());
        }

        @Override
        protected void processAnnotationOnClass(MethodMetadata data, Class<?> clz) {
            RequestMapping classAnnotation = findMergedAnnotation(clz, RequestMapping.class);
            if (classAnnotation != null) {
                // Prepend path from class annotation if specified
                if (classAnnotation.value().length > 0) {
                    String pathValue = emptyToNull(classAnnotation.value()[0]);
                    if (pathValue != null && !pathValue.endsWith("/")) {
                        pathValue = pathValue + "/";
                    }
                    data.template().uri(pathValue);
                }
            }
        }
    }
}
