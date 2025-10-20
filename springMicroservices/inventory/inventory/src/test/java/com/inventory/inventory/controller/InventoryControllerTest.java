package com.inventory.inventory.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.jasminb.jsonapi.JSONAPIDocument;
import com.github.jasminb.jsonapi.ResourceConverter;
import com.inventory.inventory.dto.inventory.InventoryDto;
import com.inventory.inventory.dto.inventory.InventoryRequest;
import com.inventory.inventory.dto.purchase.PurchaseDto;
import com.inventory.inventory.exception.InsufficientStockException;
import com.inventory.inventory.exception.InventoryByProductUuidNotFoundException;
import com.inventory.inventory.exception.ProductServiceException;
import com.inventory.inventory.exception.UuidInvalidException;
import com.inventory.inventory.service.inventory.InventoryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.client.ResourceAccessException;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(InventoryController.class)
class InventoryControllerTest {

    private static final String JSONAPI_CONTENT_TYPE = "application/vnd.api+json";
    private static final String AUTH_TOKEN = "Xf9G7qP3vL2R8kZs4nM1bYwQ6aT0jH5eC8dV3rL9pF2uN7xK1mBzA4sQ6yW0tE3o";

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private InventoryService inventoryService;

    @MockBean
    private ResourceConverter resourceConverter;

    private ObjectMapper objectMapper;
    private InventoryDto inventoryDto;
    private InventoryRequest inventoryRequest;
    private UUID productId;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        productId = UUID.randomUUID();

        inventoryDto = new InventoryDto();
        inventoryDto.setProductId(productId);
        inventoryDto.setQuantity(10);

        inventoryRequest = new InventoryRequest();
        inventoryRequest.setProductId(productId);
        inventoryRequest.setQuantity(10);
    }

    @Test
    void createInventory_Success() throws Exception {
        when(inventoryService.createInventory(any(InventoryRequest.class))).thenReturn(inventoryDto);
        when(resourceConverter.writeDocument(any(JSONAPIDocument.class))).thenReturn(new byte[]{});

        mockMvc.perform(post("/v1/inventory")
                .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + AUTH_TOKEN)
                .content(objectMapper.writeValueAsString(inventoryRequest)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Content-Type", JSONAPI_CONTENT_TYPE));
    }

    @Test
    void createInventory_InvalidRequest() throws Exception {
        InventoryRequest invalidRequest = new InventoryRequest();


        mockMvc.perform(post("/v1/inventory")
                .contentType(JSONAPI_CONTENT_TYPE)
                .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createInventory_ProductNotFound() throws Exception {
        when(inventoryService.createInventory(any(InventoryRequest.class)))
                .thenThrow(new InventoryByProductUuidNotFoundException(productId));

        mockMvc.perform(post("/v1/inventory")
                        .header("Authorization", "Bearer " + AUTH_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(inventoryRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createInventory_ProductServiceUnavailable() throws Exception {
        when(inventoryService.createInventory(any(InventoryRequest.class)))
                .thenThrow(new ProductServiceException(new ResourceAccessException("Service unavailable")));

        mockMvc.perform(post("/v1/inventory")
                        .header("Authorization", "Bearer " + AUTH_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)  // <-- Cambiar aquí
                        .content(objectMapper.writeValueAsString(inventoryRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getInventory_Success() throws Exception {
        when(inventoryService.getInventoryWithProduct(productId.toString())).thenReturn(inventoryDto);
        when(resourceConverter.writeDocument(any(JSONAPIDocument.class))).thenReturn(new byte[]{});

        mockMvc.perform(get("/v1/inventory/" + productId)
                        .header("Authorization", "Bearer " + AUTH_TOKEN)
                .accept(JSONAPI_CONTENT_TYPE))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", JSONAPI_CONTENT_TYPE));
    }

    @Test
    void getInventory_NotFound() throws Exception {
        when(inventoryService.getInventoryWithProduct(productId.toString()))
                .thenThrow(new InventoryByProductUuidNotFoundException(productId));

        mockMvc.perform(get("/v1/inventory/" + productId)
                        .header("Authorization", "Bearer " + AUTH_TOKEN)
                .accept(JSONAPI_CONTENT_TYPE))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getInventory_InvalidUUID() throws Exception {
        when(inventoryService.getInventoryWithProduct("invalid-uuid"))
                .thenThrow(new UuidInvalidException("invalid-uuid"));

        mockMvc.perform(get("/v1/inventory/invalid-uuid")
                        .header("Authorization", "Bearer " + AUTH_TOKEN)
                        .accept(JSONAPI_CONTENT_TYPE))
                .andExpect(status().isBadRequest());
    }

    @Test
    void processPurchase_Success() throws Exception {
        PurchaseDto purchaseDto = new PurchaseDto();
        purchaseDto.setQuantitySold(5);

        when(inventoryService.processPurchase(eq(productId.toString()), any(PurchaseDto.class)))
                .thenReturn(inventoryDto);
        when(resourceConverter.writeDocument(any(JSONAPIDocument.class))).thenReturn(new byte[]{});

        mockMvc.perform(patch("/v1/inventory/" + productId + "/purchase")
                        .header("Authorization", "Bearer " + AUTH_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(purchaseDto)))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", JSONAPI_CONTENT_TYPE));
    }

    @Test
    void processPurchase_InsufficientStock() throws Exception {
        PurchaseDto purchaseDto = new PurchaseDto();
        purchaseDto.setQuantitySold(15);

        when(inventoryService.processPurchase(eq(productId.toString()), any(PurchaseDto.class)))
                .thenThrow(new InsufficientStockException(10, 15));

        mockMvc.perform(patch("/v1/inventory/" + productId + "/purchase")
                .contentType(JSONAPI_CONTENT_TYPE)
                .content(objectMapper.writeValueAsString(purchaseDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void processPurchase_InvalidQuantity() throws Exception {
        PurchaseDto purchaseDto = new PurchaseDto();
        purchaseDto.setQuantitySold(-1);

        mockMvc.perform(patch("/v1/inventory/" + productId + "/purchase")
                .contentType(JSONAPI_CONTENT_TYPE)
                .content(objectMapper.writeValueAsString(purchaseDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void processPurchase_ProductNotFound() throws Exception {
        PurchaseDto purchaseDto = new PurchaseDto();
        purchaseDto.setQuantitySold(5);

        when(inventoryService.processPurchase(eq(productId.toString()), any(PurchaseDto.class)))
                .thenThrow(new InventoryByProductUuidNotFoundException(productId));

        mockMvc.perform(patch("/v1/inventory/" + productId + "/purchase")
                        .header("Authorization", "Bearer " + AUTH_TOKEN)  // <-- Añadir esto
                        .contentType(MediaType.APPLICATION_JSON)  // <-- Cambiar esto
                        .content(objectMapper.writeValueAsString(purchaseDto)))
                .andExpect(status().isNotFound());
    }

    @Test
    void processPurchase_ProductServiceError() throws Exception {
        PurchaseDto purchaseDto = new PurchaseDto();
        purchaseDto.setQuantitySold(5);

        when(inventoryService.processPurchase(eq(productId.toString()), any(PurchaseDto.class)))
                .thenThrow(new ProductServiceException(new Exception("Service error")));

        mockMvc.perform(patch("/v1/inventory/" + productId + "/purchase")
                        .header("Authorization", "Bearer " + AUTH_TOKEN)  // <-- Añadir esto
                        .contentType(MediaType.APPLICATION_JSON)  // <-- Cambiar esto
                        .content(objectMapper.writeValueAsString(purchaseDto)))
                .andExpect(status().isInternalServerError());  // <-- Cambiar de 503 a 500
    }
}