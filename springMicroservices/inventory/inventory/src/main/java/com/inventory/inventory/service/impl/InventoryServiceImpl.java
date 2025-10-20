package com.inventory.inventory.service.impl;

import com.github.jasminb.jsonapi.JSONAPIDocument;
import com.github.jasminb.jsonapi.ResourceConverter;
import com.github.jasminb.jsonapi.exceptions.DocumentSerializationException;
import com.inventory.inventory.client.ProductClient;
import com.inventory.inventory.dto.inventory.InventoryDto;
import com.inventory.inventory.dto.inventory.InventoryMapper;
import com.inventory.inventory.dto.inventory.InventoryRequest;
import com.inventory.inventory.dto.product.ProductDto;
import com.inventory.inventory.dto.purchase.PurchaseDto;
import com.inventory.inventory.exception.InsufficientStockException;
import com.inventory.inventory.exception.InventoryByProductUuidNotFoundException;
import com.inventory.inventory.exception.ProductDeserializationException;
import com.inventory.inventory.exception.ProductServiceException;
import com.inventory.inventory.exception.UuidInvalidException;
import com.inventory.inventory.model.Inventory;
import com.inventory.inventory.repository.InventoryRepository;
import com.inventory.inventory.service.InventoryService;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Optional;
import java.util.UUID;

@Service
@AllArgsConstructor
public class InventoryServiceImpl implements InventoryService {

    private final InventoryRepository inventoryRepository;
    private final InventoryMapper inventoryMapper;
    private final ProductClient productClient;
    private final ResourceConverter resourceConverter;

    private ProductDto getProductDto(UUID productId) {
        byte[] responseProduct = productClient.getProductById(productId);
        try {
            JSONAPIDocument<ProductDto> document = resourceConverter.readDocument(responseProduct, ProductDto.class);
            return document.get();
        } catch (Exception e) {
            throw new ProductServiceException(e);
        }
    }

    @Override
    public InventoryDto createInventory(InventoryRequest inventoryRequest) {
        UUID idProduct = inventoryRequest.getProductId();
        Optional<Inventory> inventoryExists = inventoryRepository.findByProductId(idProduct);

        if(inventoryExists.isPresent()){
            return updateQuantity(idProduct.toString(), inventoryRequest);
        }

        Inventory inventory = inventoryMapper.toModel(inventoryRequest);
        inventory = inventoryRepository.save(inventory);

        InventoryDto inventoryDto = inventoryMapper.toDto(inventory);
        inventoryDto.setProduct(getProductDto(idProduct));

        return inventoryDto;
    }



    @Override
    public InventoryDto getInventoryWithProduct(String productId) {
        UUID idProduct;
        try {
            idProduct = UUID.fromString(productId);
        } catch (IllegalArgumentException e) {
            throw new UuidInvalidException(productId);
        }

        Inventory inventory = inventoryRepository.findByProductId(idProduct)
                .orElseThrow(() -> new InventoryByProductUuidNotFoundException(idProduct));

        InventoryDto inventoryDto = inventoryMapper.toDto(inventory);
        inventoryDto.setProduct(getProductDto(idProduct));

        return inventoryDto;
    }

    @Override
    public InventoryDto updateQuantity(String idProduct, InventoryRequest inventoryRequest) {
        UUID productId;
        try {
            productId = UUID.fromString(idProduct);
        } catch (IllegalArgumentException e) {
            throw new UuidInvalidException(idProduct);
        }

        Inventory inventory = inventoryRepository.findByProductId(productId)
                .orElseThrow(() -> new InventoryByProductUuidNotFoundException(productId));

        inventoryMapper.updateModel(inventoryRequest, inventory);
        InventoryDto inventoryResponse = inventoryMapper.toDto(inventoryRepository.save(inventory));
        inventoryResponse.setProduct(getProductDto(inventoryResponse.getProductId()));
        return inventoryResponse;
    }

    @Override
    public InventoryDto processPurchase(String idProduct, PurchaseDto purchaseDto) {
        UUID productId;
        try {
            productId = UUID.fromString(idProduct);
        } catch (IllegalArgumentException e) {
            throw new UuidInvalidException(idProduct);
        }


        Inventory inventory = inventoryRepository.findByProductId(productId)
                .orElseThrow(() -> new InventoryByProductUuidNotFoundException(productId));

        // Validar stock suficiente
        if (inventory.getQuantity() < purchaseDto.getQuantitySold()) {
            throw new InsufficientStockException(inventory.getQuantity(), purchaseDto.getQuantitySold());
        }

        inventory.setQuantity(inventory.getQuantity() - purchaseDto.getQuantitySold());
        inventory = inventoryRepository.save(inventory);
        
        InventoryDto inventoryDto = inventoryMapper.toDto(inventory);
        inventoryDto.setProduct(getProductDto(productId));

        return inventoryDto;
    }
}
