package com.inventory.inventory.exception;

import com.github.jasminb.jsonapi.exceptions.DocumentSerializationException;

import java.io.Serializable;

public class ProductServiceException extends RuntimeException implements Serializable {
    public ProductServiceException(Exception e) {
        super("\"Error communicating with product service" + e);
    }

}
