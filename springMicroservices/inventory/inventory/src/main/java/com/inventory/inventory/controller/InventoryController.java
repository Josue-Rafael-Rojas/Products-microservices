package com.inventory.inventory.controller;

import com.github.jasminb.jsonapi.JSONAPIDocument;
import com.github.jasminb.jsonapi.ResourceConverter;
import com.github.jasminb.jsonapi.exceptions.DocumentSerializationException;
import com.inventory.inventory.dto.inventory.InventoryDto;
import com.inventory.inventory.dto.inventory.InventoryRequest;
import com.inventory.inventory.dto.purchase.PurchaseDto;
import com.inventory.inventory.exception.UuidInvalidException;
import com.inventory.inventory.model.Inventory;
import com.inventory.inventory.service.InventoryService;
import jakarta.persistence.Access;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/v1/inventory")
@AllArgsConstructor
public class InventoryController {

    private final InventoryService inventoryService;
    private final ResourceConverter resourceConverter;


    @PostMapping(
            consumes = "application/json",
            produces = "application/vnd.api+json"
    )
    public ResponseEntity<byte[]> createClient(@RequestBody @Valid InventoryRequest inventoryRequest) {
        try {
            // 1. Service retorna ProductDto
            InventoryDto createdInventory = inventoryService.createInventory(inventoryRequest);

            // 2. Serializar ProductDto → JSON:API
            JSONAPIDocument<InventoryDto> responseDoc = new JSONAPIDocument<>(createdInventory);

            byte[] jsonApiResponse = resourceConverter.writeDocument(responseDoc);

            // 3. Retornar
            return ResponseEntity
                    .status(201)
                    .header("Content-Type", "application/vnd.api+json")
                    .body(jsonApiResponse);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().build();
        }
    }


    @GetMapping(
            value = "/{productId}",
            produces = "application/vnd.api+json"
    )
    public ResponseEntity<byte[]> getInventoryWithProduct(@PathVariable String productId) {
        try {
            InventoryDto inventory = inventoryService.getInventoryWithProduct(productId);

            // El ResourceConverter manejará automáticamente la sección included
            JSONAPIDocument<InventoryDto> responseDoc = new JSONAPIDocument<>(inventory);
            byte[] jsonApiResponse = resourceConverter.writeDocument(responseDoc);

            return ResponseEntity
                    .ok()
                    .header("Content-Type", "application/vnd.api+json")
                    .body(jsonApiResponse);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().build();
        }
    }

    @PatchMapping(
            value = "/{productId}/purchase",
            consumes = "application/json",
            produces = "application/vnd.api+json"
    )
    public ResponseEntity<byte[]> processPurchase(
            @PathVariable String productId,
            @RequestBody @Valid PurchaseDto purchaseDto) {
        InventoryDto updatedInventory = inventoryService.processPurchase(productId, purchaseDto);
        try {
            JSONAPIDocument<InventoryDto> responseDoc = new JSONAPIDocument<>(updatedInventory);
            byte[] jsonApiResponse = resourceConverter.writeDocument(responseDoc);

            return ResponseEntity
                    .ok()
                    .header("Content-Type", "application/vnd.api+json")
                    .body(jsonApiResponse);
        } catch (DocumentSerializationException e) { // Solo capturar excepciones específicas
            e.printStackTrace();
            return ResponseEntity.badRequest().build();
        }
    }

}
