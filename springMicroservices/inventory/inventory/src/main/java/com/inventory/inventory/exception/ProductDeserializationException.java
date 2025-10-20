package com.inventory.inventory.exception;

import com.github.jasminb.jsonapi.exceptions.DocumentSerializationException;

import java.io.IOException;
import java.io.Serializable;
import java.util.UUID;

public class ProductDeserializationException extends RuntimeException implements Serializable {
    public ProductDeserializationException(IOException e) {
        super("Error reading product data" + e);
    }

}
