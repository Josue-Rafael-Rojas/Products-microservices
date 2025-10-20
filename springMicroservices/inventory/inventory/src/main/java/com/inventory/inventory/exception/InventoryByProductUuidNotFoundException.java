package com.inventory.inventory.exception;

import java.io.Serializable;
import java.util.UUID;

public class InventoryByProductUuidNotFoundException extends RuntimeException implements Serializable {

    public InventoryByProductUuidNotFoundException(UUID uuid) {
        super("Inventory not found with product id: " + uuid);
    }
}
