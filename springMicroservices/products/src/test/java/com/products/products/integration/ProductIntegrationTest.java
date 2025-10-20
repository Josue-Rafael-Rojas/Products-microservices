package com.products.products.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.products.products.dto.product.ProductRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.math.BigDecimal;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class ProductIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Value("${app.tokenSecurity}")
    private String securityToken;

    private final String CONTENT_TYPE = "application/vnd.api+json";

    @Test
    public void createProduct_Success() throws Exception {
        ProductRequest request = new ProductRequest();
        request.setName("Test Product");
        request.setPrice(new BigDecimal("99.99"));

        mockMvc.perform(post("/v1/products")
                .header("Authorization", "Bearer " + securityToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Content-Type", CONTENT_TYPE))
                .andExpect(jsonPath("$.data.type").value("product"))
                .andExpect(jsonPath("$.data.attributes.name").value("Test Product"))
                .andExpect(jsonPath("$.data.attributes.price").value(99.99));
    }

    @Test
    public void createProduct_InvalidData() throws Exception {
        ProductRequest request = new ProductRequest(); // Sin datos obligatorios

        mockMvc.perform(post("/v1/products")
                .header("Authorization", "Bearer " + securityToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void getAllProducts_Success() throws Exception {
        // Primero creamos algunos productos
        createTestProduct("Product 1", "10.99");
        createTestProduct("Product 2", "20.99");

        mockMvc.perform(get("/v1/products")
                .header("Authorization", "Bearer " + securityToken)
                .accept(MediaType.parseMediaType(CONTENT_TYPE)))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", CONTENT_TYPE))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(2))
                .andExpect(jsonPath("$.meta.totalElements").value(2))
                .andExpect(jsonPath("$.meta.totalPages").value(1))
                .andExpect(jsonPath("$.meta.number").value(0))
                .andExpect(jsonPath("$.meta.size").value(10));
    }

    @Test
    public void getProductById_Success() throws Exception {
        // Primero creamos un producto
        MvcResult createResult = createTestProduct("Test Product", "15.99");
        String responseContent = createResult.getResponse().getContentAsString();
        String productId = extractProductIdFromResponse(responseContent);

        mockMvc.perform(get("/v1/products/" + productId)
                .header("Authorization", "Bearer " + securityToken)
                .accept(MediaType.parseMediaType(CONTENT_TYPE)))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", CONTENT_TYPE))
                .andExpect(jsonPath("$.data.id").value(productId))
                .andExpect(jsonPath("$.data.attributes.name").value("Test Product"))
                .andExpect(jsonPath("$.data.attributes.price").value(15.99));
    }

    @Test
    public void getProductById_NotFound() throws Exception {
        String nonExistentId = "00000000-0000-0000-0000-000000000000";
        
        mockMvc.perform(get("/v1/products/" + nonExistentId)
                .header("Authorization", "Bearer " + securityToken)
                .accept(MediaType.parseMediaType(CONTENT_TYPE)))
                .andExpect(status().isNotFound());
    }

    @Test
    public void updateProduct_Success() throws Exception {
        // Primero creamos un producto
        MvcResult createResult = createTestProduct("Original Name", "10.99");
        String responseContent = createResult.getResponse().getContentAsString();
        String productId = extractProductIdFromResponse(responseContent);

        // Actualizamos el producto
        ProductRequest updateRequest = new ProductRequest();
        updateRequest.setName("Updated Name");
        updateRequest.setPrice(new BigDecimal("20.99"));

        mockMvc.perform(patch("/v1/products/" + productId)
                .header("Authorization", "Bearer " + securityToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", CONTENT_TYPE))
                .andExpect(jsonPath("$.data.id").value(productId))
                .andExpect(jsonPath("$.data.attributes.name").value("Updated Name"))
                .andExpect(jsonPath("$.data.attributes.price").value(20.99));

        // Verificamos que el producto fue actualizado
        mockMvc.perform(get("/v1/products/" + productId)
                .header("Authorization", "Bearer " + securityToken)
                .accept(MediaType.parseMediaType(CONTENT_TYPE)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.attributes.name").value("Updated Name"))
                .andExpect(jsonPath("$.data.attributes.price").value(20.99));
    }

    @Test
    public void deleteProduct_Success() throws Exception {
        // Primero creamos un producto
        MvcResult createResult = createTestProduct("Product to Delete", "30.99");
        String responseContent = createResult.getResponse().getContentAsString();
        String productId = extractProductIdFromResponse(responseContent);

        // Eliminamos el producto
        mockMvc.perform(delete("/v1/products/" + productId)
                .header("Authorization", "Bearer " + securityToken))
                .andExpect(status().isNoContent());

        // Verificamos que el producto ya no existe
        mockMvc.perform(get("/v1/products/" + productId)
                .header("Authorization", "Bearer " + securityToken)
                .accept(MediaType.parseMediaType(CONTENT_TYPE)))
                .andExpect(status().isNotFound());
    }

    private MvcResult createTestProduct(String name, String price) throws Exception {
        ProductRequest request = new ProductRequest();
        request.setName(name);
        request.setPrice(new BigDecimal(price));

        return mockMvc.perform(post("/v1/products")
                .header("Authorization", "Bearer " + securityToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn();
    }

    private String extractProductIdFromResponse(String response) throws Exception {
        return objectMapper.readTree(response)
                .path("data")
                .path("id")
                .asText();
    }
}