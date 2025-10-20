package com.inventory.inventory.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.github.jasminb.jsonapi.ResourceConverter;
import com.inventory.inventory.dto.inventory.InventoryDto;
import com.inventory.inventory.dto.product.ProductDto;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JsonApiConfig {


    @Bean
    public ResourceConverter resourceConverter() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);


        ResourceConverter converter = new ResourceConverter(
                objectMapper,
                InventoryDto.class,
                ProductDto.class
        );
        converter.enableSerializationOption(com.github.jasminb.jsonapi.SerializationFeature.INCLUDE_LINKS);


        converter.enableSerializationOption(com.github.jasminb.jsonapi.SerializationFeature.INCLUDE_META);

        converter.enableSerializationOption(com.github.jasminb.jsonapi.SerializationFeature.INCLUDE_RELATIONSHIP_ATTRIBUTES);

        return converter;
    }
}
