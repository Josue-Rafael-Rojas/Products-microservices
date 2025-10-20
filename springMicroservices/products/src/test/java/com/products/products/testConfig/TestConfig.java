package com.products.products.testConfig;

import com.github.jasminb.jsonapi.ResourceConverter;
import com.products.products.dto.product.ProductDto;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

@TestConfiguration
public class TestConfig {
    
    @Bean
    @Primary
    public ResourceConverter resourceConverter() {
        ResourceConverter resourceConverter = new ResourceConverter(ProductDto.class);
        return resourceConverter;
    }
}