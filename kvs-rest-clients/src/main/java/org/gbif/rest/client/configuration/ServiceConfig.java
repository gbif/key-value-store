package org.gbif.rest.client.configuration;

import org.gbif.rest.client.species.NameUsageMatchService;
import org.springframework.beans.factory.config.YamlPropertiesFactoryBean;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.io.ClassPathResource;

@Configuration
@EnableAutoConfiguration
@ComponentScan(basePackages = {
        "org.gbif.rest.client.species", "org.gbif.rest.client.grscicoll", "org.gbif.rest.client.geocode"})
@EnableFeignClients(basePackages = {
        "org.gbif.rest.client.species", "org.gbif.rest.client.grscicoll", "org.gbif.rest.client.geocode"})
public class ServiceConfig {

    NameUsageMatchService nameUsageMatchService;

    @Bean
    public NameUsageMatchService nameUsageMatchService() {
        return nameUsageMatchService;
    }

    @Bean
    public static PropertySourcesPlaceholderConfigurer properties() {
        PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer = new PropertySourcesPlaceholderConfigurer();
        YamlPropertiesFactoryBean yaml = new YamlPropertiesFactoryBean();
        yaml.setResources(new ClassPathResource("application.yml"));
        propertySourcesPlaceholderConfigurer.setProperties(yaml.getObject());
        return propertySourcesPlaceholderConfigurer;
    }
}