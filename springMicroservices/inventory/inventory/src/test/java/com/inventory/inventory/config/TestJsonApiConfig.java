package com.inventory.inventory.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.github.jasminb.jsonapi.ResourceConverter;
import com.github.jasminb.jsonapi.SerializationFeature;
import com.inventory.inventory.dto.abstracts.BaseDto;
import com.inventory.inventory.dto.inventory.InventoryDto;
import com.inventory.inventory.dto.inventory.InventoryRequest;
import com.inventory.inventory.dto.product.ProductDto;
import com.inventory.inventory.dto.purchase.PurchaseDto;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.servlet.config.annotation.ContentNegotiationConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import java.util.Arrays;
import java.util.List;

@TestConfiguration
@Profile("test")
public class TestJsonApiConfig implements WebMvcConfigurer {

    @Override
    public void configureContentNegotiation(ContentNegotiationConfigurer configurer) {
        configurer
            .defaultContentType(MediaType.valueOf("application/vnd.api+json"))
            .mediaType("json-api", MediaType.valueOf("application/vnd.api+json"))
            .useRegisteredExtensionsOnly(false)
            .favorParameter(false);
    }

    @Bean
    public ObjectMapper objectMapper() {
        return Jackson2ObjectMapperBuilder.json()
            .build();
    }

    @Bean
    @Primary
    public ResourceConverter resourceConverter(ObjectMapper objectMapper) {
        ResourceConverter converter = new ResourceConverter(
                objectMapper,
                BaseDto.class,
                InventoryDto.class,
                ProductDto.class,
                InventoryRequest.class,
                PurchaseDto.class
        );

        // Habilitar las mismas opciones que en producción
        converter.enableSerializationOption(SerializationFeature.INCLUDE_LINKS);
        converter.enableSerializationOption(SerializationFeature.INCLUDE_META);
        converter.enableSerializationOption(SerializationFeature.INCLUDE_RELATIONSHIP_ATTRIBUTES);

        return converter;
    }

    @Bean
    public HttpMessageConverter<?> jsonApiMessageConverter() {
        MappingJackson2HttpMessageConverter converter = new MappingJackson2HttpMessageConverter();
        converter.setSupportedMediaTypes(Arrays.asList(MediaType.valueOf("application/vnd.api+json")));
        return converter;
    }

    @Override
    public void configureMessageConverters(List<HttpMessageConverter<?>> converters) {
        converters.add(jsonApiMessageConverter());
    }
}