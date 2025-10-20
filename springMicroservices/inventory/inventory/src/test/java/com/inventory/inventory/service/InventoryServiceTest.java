package com.inventory.inventory.service;

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
import com.inventory.inventory.model.Inventory;
import com.inventory.inventory.repository.InventoryRepository;
import com.inventory.inventory.service.inventory.impl.InventoryServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.ResourceAccessException;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InventoryServiceTest {

    @Mock
    private InventoryRepository inventoryRepository;

    @Mock
    private InventoryMapper inventoryMapper;

    @Mock
    private ProductClient productClient;

    @Mock
    private ResourceConverter resourceConverter;

    @InjectMocks
    private InventoryServiceImpl inventoryService;

    private Inventory inventory;
    private InventoryRequest inventoryRequest;
    private InventoryDto inventoryDto;
    private ProductDto productDto;
    private UUID productId;
    private byte[] productResponse;

    @BeforeEach
    void setUp() {
        productId = UUID.randomUUID();
        
        inventory = new Inventory();
        inventory.setProductId(productId);
        inventory.setQuantity(10);

        inventoryRequest = new InventoryRequest();
        inventoryRequest.setProductId(productId);
        inventoryRequest.setQuantity(10);

        inventoryDto = new InventoryDto();
        inventoryDto.setProductId(productId);
        inventoryDto.setQuantity(10);

        productDto = new ProductDto();
        productDto.setUuid(productId.toString());
        productDto.setName("Test Product");

        productResponse = new byte[]{1, 2, 3}; // Mock response
    }

   @Test
   void createInventory_Success() throws Exception {
       when(inventoryRepository.findByProductId(productId)).thenReturn(Optional.empty());
       when(inventoryMapper.toModel(inventoryRequest)).thenReturn(inventory);
       when(inventoryRepository.save(any(Inventory.class))).thenReturn(inventory);
       when(productClient.getProductById(eq(productId), anyString())).thenReturn(productResponse);
       when(resourceConverter.readDocument(productResponse, ProductDto.class))
               .thenReturn(new JSONAPIDocument<>(productDto));
       when(inventoryMapper.toDto(inventory)).thenReturn(inventoryDto);

       InventoryDto result = inventoryService.createInventory(inventoryRequest);

       assertThat(result).isNotNull();
       assertThat(result.getProductId()).isEqualTo(productId);
       assertThat(result.getQuantity()).isEqualTo(inventoryRequest.getQuantity());
       verify(productClient, times(1)).getProductById(eq(productId), anyString());
   }

    @Test
    void createInventory_ProductServiceError() {
        when(productClient.getProductById(eq(productId), anyString()))
                .thenThrow(new ResourceAccessException("Service unavailable"));

        assertThatThrownBy(() ->
                inventoryService.createInventory(inventoryRequest)
        ).isInstanceOf(ProductServiceException.class)
                .hasMessageContaining("Error communicating with product service");

        verify(inventoryRepository, never()).save(any());
        verify(inventoryRepository, never()).findByProductId(any());
    }

    @Test
    void createInventory_DuplicateProduct() throws Exception {
        when(inventoryRepository.findByProductId(productId)).thenReturn(Optional.of(inventory));
        when(productClient.getProductById(eq(productId), anyString())).thenReturn(productResponse);
        when(resourceConverter.readDocument(productResponse, ProductDto.class))
                .thenReturn(new JSONAPIDocument<>(productDto));
        when(inventoryMapper.toDto(any())).thenReturn(inventoryDto);
        when(inventoryRepository.save(any())).thenReturn(inventory);


        InventoryDto result = inventoryService.createInventory(inventoryRequest);

        assertThat(result).isNotNull();
        assertThat(result.getProductId()).isEqualTo(productId);

        verify(inventoryMapper).updateModel(eq(inventoryRequest), eq(inventory));
        verify(inventoryRepository).save(inventory);
    }

    @Test
    void getInventoryWithProduct_Success() throws Exception {
        when(inventoryRepository.findByProductId(productId)).thenReturn(Optional.of(inventory));
        when(productClient.getProductById(eq(productId), anyString())).thenReturn(productResponse);
        when(resourceConverter.readDocument(productResponse, ProductDto.class))
                .thenReturn(new JSONAPIDocument<>(productDto));
        when(inventoryMapper.toDto(inventory)).thenReturn(inventoryDto);

        InventoryDto result = inventoryService.getInventoryWithProduct(productId.toString());

        assertThat(result).isNotNull();
        assertThat(result.getProductId()).isEqualTo(productId);
        verify(productClient, times(1)).getProductById(eq(productId), anyString());
    }

    @Test
    void getInventoryWithProduct_NotFound() {
        when(inventoryRepository.findByProductId(productId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> 
            inventoryService.getInventoryWithProduct(productId.toString())
        ).isInstanceOf(InventoryByProductUuidNotFoundException.class)
         .hasMessageContaining(productId.toString());

        verify(productClient, never()).getProductById(any(), anyString());
    }

   @Test
   void processPurchase_Success() throws Exception {
       PurchaseDto purchaseDto = new PurchaseDto();
       purchaseDto.setQuantitySold(5);

       Inventory updatedInventory = new Inventory();
       updatedInventory.setProductId(productId);
       updatedInventory.setQuantity(5); // 10 - 5

       InventoryDto updatedInventoryDto = new InventoryDto();
       updatedInventoryDto.setProductId(productId);
       updatedInventoryDto.setQuantity(5);

       when(inventoryRepository.findByProductId(productId)).thenReturn(Optional.of(inventory));
       when(inventoryRepository.save(any(Inventory.class))).thenReturn(updatedInventory);
       when(productClient.getProductById(eq(productId), anyString())).thenReturn(productResponse);
       when(resourceConverter.readDocument(productResponse, ProductDto.class))
               .thenReturn(new JSONAPIDocument<>(productDto));
       when(inventoryMapper.toDto(updatedInventory)).thenReturn(updatedInventoryDto);

       InventoryDto result = inventoryService.processPurchase(productId.toString(), purchaseDto);

       assertThat(result).isNotNull();
       assertThat(result.getQuantity()).isEqualTo(5);
       verify(inventoryRepository, times(1)).save(any());
       verify(productClient, times(1)).getProductById(eq(productId), anyString());
   }


    @Test
    void processPurchase_InsufficientStock() {
        PurchaseDto purchaseDto = new PurchaseDto();
        purchaseDto.setQuantitySold(15);

        when(productClient.getProductById(eq(productId), anyString())).thenReturn(productResponse);
        when(resourceConverter.readDocument(productResponse, ProductDto.class))
                .thenReturn(new JSONAPIDocument<>(productDto));

        when(inventoryRepository.findByProductId(productId)).thenReturn(Optional.of(inventory));

        assertThatThrownBy(() ->
                inventoryService.processPurchase(productId.toString(), purchaseDto)
        ).isInstanceOf(InsufficientStockException.class);

        verify(inventoryRepository, never()).save(any());
    }

    @Test
    void processPurchase_ProductNotFound() {
        PurchaseDto purchaseDto = new PurchaseDto();
        purchaseDto.setQuantitySold(5);

        when(productClient.getProductById(eq(productId), anyString())).thenReturn(productResponse);
        when(resourceConverter.readDocument(productResponse, ProductDto.class))
                .thenReturn(new JSONAPIDocument<>(productDto));


        when(inventoryRepository.findByProductId(productId)).thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                inventoryService.processPurchase(productId.toString(), purchaseDto)
        ).isInstanceOf(InventoryByProductUuidNotFoundException.class)
                .hasMessageContaining(productId.toString());

        verify(inventoryRepository, never()).save(any());
    }

    @Test
    void processPurchase_ProductServiceError() throws Exception {
        PurchaseDto purchaseDto = new PurchaseDto();
        purchaseDto.setQuantitySold(5);

        when(productClient.getProductById(eq(productId), anyString()))
                .thenThrow(new ResourceAccessException("Service unavailable"));

        assertThatThrownBy(() ->
                inventoryService.processPurchase(productId.toString(), purchaseDto)
        ).isInstanceOf(ProductServiceException.class)
                .hasMessageContaining("Error communicating with product service");

        verify(inventoryRepository, never()).save(any());
        verify(inventoryRepository, never()).findByProductId(any());
    }
}