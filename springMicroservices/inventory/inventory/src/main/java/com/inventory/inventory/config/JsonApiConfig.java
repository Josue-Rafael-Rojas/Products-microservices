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

  /*  @Bean
    public ResourceConverter resourceConverter() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        return new ResourceConverter(objectMapper,
                InventoryDto.class
        );
    }*/



    @Bean
    public ResourceConverter resourceConverter() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        // Crear el converter con ambos tipos de recursos
        ResourceConverter converter = new ResourceConverter(
                objectMapper,
                InventoryDto.class,
                ProductDto.class
        );
        // Asegurar que los links se incluyan
        converter.enableSerializationOption(com.github.jasminb.jsonapi.SerializationFeature.INCLUDE_LINKS);

        // Asegurar que el meta se incluya
        converter.enableSerializationOption(com.github.jasminb.jsonapi.SerializationFeature.INCLUDE_META);

        // Habilitar la serialización de relaciones
        converter.enableSerializationOption(com.github.jasminb.jsonapi.SerializationFeature.INCLUDE_RELATIONSHIP_ATTRIBUTES);

        return converter;
    }
}
