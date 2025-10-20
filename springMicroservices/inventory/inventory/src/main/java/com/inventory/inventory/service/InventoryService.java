package com.inventory.inventory.service;

import com.inventory.inventory.dto.inventory.InventoryDto;
import com.inventory.inventory.dto.inventory.InventoryRequest;
import com.inventory.inventory.dto.purchase.PurchaseDto;

public interface InventoryService {
    InventoryDto createInventory (InventoryRequest inventoryRequest);
    InventoryDto getInventoryWithProduct(String productId);
    InventoryDto processPurchase(String idProduct, PurchaseDto purchaseDto);
    InventoryDto updateQuantity(String idProduct, InventoryRequest inventoryRequest);
}
