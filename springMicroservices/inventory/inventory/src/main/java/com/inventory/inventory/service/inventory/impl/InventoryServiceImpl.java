package com.inventory.inventory.service.inventory.impl;

import com.github.jasminb.jsonapi.JSONAPIDocument;
import com.github.jasminb.jsonapi.ResourceConverter;
import com.inventory.inventory.client.ProductClient;
import com.inventory.inventory.dto.inventory.InventoryDto;
import com.inventory.inventory.dto.inventory.InventoryMapper;
import com.inventory.inventory.dto.inventory.InventoryRequest;
import com.inventory.inventory.dto.product.ProductDto;
import com.inventory.inventory.dto.purchase.PurchaseDto;
import com.inventory.inventory.exception.InsufficientStockException;
import com.inventory.inventory.exception.InventoryByProductUuidNotFoundException;
import com.inventory.inventory.exception.ProductServiceException;
import com.inventory.inventory.exception.UuidInvalidException;
import com.inventory.inventory.model.Inventory;
import com.inventory.inventory.repository.InventoryRepository;
import com.inventory.inventory.service.inventory.InventoryService;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.ResourceAccessException;

import java.util.Optional;
import java.util.UUID;

@Service
@AllArgsConstructor
public class InventoryServiceImpl implements InventoryService {

    private static final Logger logger = LoggerFactory.getLogger(InventoryServiceImpl.class);

    private final InventoryRepository inventoryRepository;
    private final InventoryMapper inventoryMapper;
    private final ProductClient productClient;
    private final ResourceConverter resourceConverter;

    private ProductDto getProductDto(UUID productId) {
        logger.info("Fetching product information from product service for ID: {}", productId);
        try {
            byte[] responseProduct = productClient.getProductById(productId, "Bearer aR4vN8xK2qT5jL1sP7mF0wD6yG3bH9cZ2uV8nE5kS0oQ7pY1rM4tW6fC3dJ9lX0");
            JSONAPIDocument<ProductDto> document = resourceConverter.readDocument(responseProduct, ProductDto.class);
            ProductDto product = document.get();
            logger.info("Successfully retrieved product: {}", product.getName());
            return product;
        } catch (ResourceAccessException e) {
            logger.error("Failed to access product service for ID: {}. Error: {}", productId, e.getMessage());
            throw new ProductServiceException(e);
        } catch (Exception e) {
            logger.error("Error fetching product with ID: {}. Error: {}", productId, e.getMessage());
            throw new ProductServiceException(e);
        }
    }

    @Override
    public InventoryDto createInventory(InventoryRequest inventoryRequest) {
        logger.info("Creating new inventory for product ID: {} with quantity: {}", 
            inventoryRequest.getProductId(), inventoryRequest.getQuantity());
        
        UUID idProduct = inventoryRequest.getProductId();
        ProductDto productDto = getProductDto(idProduct);

        Optional<Inventory> inventoryExists = inventoryRepository.findByProductId(idProduct);

        if(inventoryExists.isPresent()){
            logger.info("Inventory already exists for product ID: {}. Updating quantity instead.", idProduct);
            return updateQuantity(idProduct.toString(), inventoryRequest);
        }

        logger.info("Creating new inventory entry for product: {}", productDto.getName());
        Inventory inventory = inventoryMapper.toModel(inventoryRequest);
        inventory = inventoryRepository.save(inventory);

        InventoryDto inventoryDto = inventoryMapper.toDto(inventory);
        inventoryDto.setProduct(productDto);

        logger.info("Successfully created inventory for product: {} with quantity: {}", 
            productDto.getName(), inventory.getQuantity());
        return inventoryDto;
    }



    @Override
    public InventoryDto getInventoryWithProduct(String productId) {
        logger.info("Fetching inventory with product details for ID: {}", productId);
        UUID idProduct;
        try {
            idProduct = UUID.fromString(productId);
        } catch (IllegalArgumentException e) {
            logger.error("Invalid UUID format for product ID: {}", productId);
            throw new UuidInvalidException(productId);
        }

        Inventory inventory = inventoryRepository.findByProductId(idProduct)
                .orElseThrow(() -> {
                    logger.error("Inventory not found for product ID: {}", idProduct);
                    return new InventoryByProductUuidNotFoundException(idProduct);
                });

        logger.info("Found inventory for product ID: {} with quantity: {}", 
            idProduct, inventory.getQuantity());

        InventoryDto inventoryDto = inventoryMapper.toDto(inventory);
        inventoryDto.setProduct(getProductDto(idProduct));

        return inventoryDto;
    }

    @Override
    public InventoryDto updateQuantity(String idProduct, InventoryRequest inventoryRequest) {
        logger.info("Updating inventory quantity for product ID: {} to: {}", 
            idProduct, inventoryRequest.getQuantity());
        UUID productId;
        try {
            productId = UUID.fromString(idProduct);
        } catch (IllegalArgumentException e) {
            logger.error("Invalid UUID format for product ID: {}", idProduct);
            throw new UuidInvalidException(idProduct);
        }

        Inventory inventory = inventoryRepository.findByProductId(productId)
                .orElseThrow(() -> {
                    logger.error("Inventory not found for update with product ID: {}", productId);
                    return new InventoryByProductUuidNotFoundException(productId);
                });

        logger.info("Current inventory quantity: {}", inventory.getQuantity());
        inventoryMapper.updateModel(inventoryRequest, inventory);
        inventory = inventoryRepository.save(inventory);
        logger.info("Updated inventory quantity to: {}", inventory.getQuantity());

        InventoryDto inventoryResponse = inventoryMapper.toDto(inventory);
        inventoryResponse.setProduct(getProductDto(inventoryResponse.getProductId()));
        return inventoryResponse;
    }

    @Override
    public InventoryDto processPurchase(String idProduct, PurchaseDto purchaseDto) {
        logger.info("Processing purchase for product ID: {} with quantity: {}", 
            idProduct, purchaseDto.getQuantitySold());
        UUID productId;
        try {
            productId = UUID.fromString(idProduct);
        } catch (IllegalArgumentException e) {
            logger.error("Invalid UUID format for product ID in purchase: {}", idProduct);
            throw new UuidInvalidException(idProduct);
        }

        ProductDto productDto = getProductDto(productId);
        logger.info("Processing purchase for product: {}", productDto.getName());

        Inventory inventory = inventoryRepository.findByProductId(productId)
                .orElseThrow(() -> {
                    logger.error("Inventory not found for purchase with product ID: {}", productId);
                    return new InventoryByProductUuidNotFoundException(productId);
                });

        logger.info("Current inventory quantity: {}", inventory.getQuantity());
        // Validar stock suficiente
        if (inventory.getQuantity() < purchaseDto.getQuantitySold()) {
            logger.error("Insufficient stock for product: {}. Required: {}, Available: {}", 
                productDto.getName(), purchaseDto.getQuantitySold(), inventory.getQuantity());
            throw new InsufficientStockException(inventory.getQuantity(), purchaseDto.getQuantitySold());
        }

        inventory.setQuantity(inventory.getQuantity() - purchaseDto.getQuantitySold());
        inventory = inventoryRepository.save(inventory);
        logger.info("Updated inventory quantity after purchase: {}", inventory.getQuantity());
        
        InventoryDto inventoryDto = inventoryMapper.toDto(inventory);
        inventoryDto.setProduct(productDto);

        logger.info("Purchase processed successfully for product: {}", productDto.getName());
        return inventoryDto;
    }
}
