package com.products.products.exception;

import java.io.Serializable;
import java.util.UUID;

public class ProductNotFoundException extends RuntimeException implements Serializable {

    public ProductNotFoundException(UUID uuid) {
        super("Product not found with id: " + uuid);
    }
}
