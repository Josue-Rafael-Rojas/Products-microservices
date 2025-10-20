package com.inventory.inventory.client;

import com.inventory.inventory.dto.product.ProductDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.UUID;

@FeignClient(name = "product-service", url = "http://localhost:8080")
public interface ProductClient {

    @GetMapping(value = "/v1/products/{uuid}", produces = "application/vnd.api+json")
    byte[] getProductById(@PathVariable("uuid") UUID uuid);
}
