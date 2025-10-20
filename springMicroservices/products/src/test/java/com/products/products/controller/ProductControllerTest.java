package com.products.products.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.jasminb.jsonapi.JSONAPIDocument;
import com.github.jasminb.jsonapi.ResourceConverter;
import com.products.products.dto.product.ProductDto;
import com.products.products.dto.product.ProductRequest;
import com.products.products.service.interceptor.AuthInterceptor;
import com.products.products.service.product.ProductService;
import com.products.products.testConfig.TestConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ProductController.class)
@ActiveProfiles("test")
@Import(TestConfig.class)
class ProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ProductService productService;

    @Autowired
    private ResourceConverter resourceConverter;

    @MockBean
    private AuthInterceptor authInterceptor;

    private final String securityToken = "Bearer test-token-123";
    private final String CONTENT_TYPE = "application/vnd.api+json";

    @BeforeEach
    void setUp() throws Exception {
        when(authInterceptor.preHandle(any(), any(), any())).thenReturn(true);
    }

    private ProductDto createMockProductDto() {
        ProductDto dto = new ProductDto();
        dto.setUuid(UUID.randomUUID().toString());
        dto.setName("Test Product");
        dto.setPrice(new BigDecimal("99.99"));
        return dto;
    }

    @Test
    void createProduct_Success() throws Exception {
        ProductRequest request = new ProductRequest();
        request.setName("Test Product");
        request.setPrice(new BigDecimal("99.99"));

        ProductDto mockResponse = createMockProductDto();
        when(productService.createProduct(any(ProductRequest.class))).thenReturn(mockResponse);


        mockMvc.perform(post("/v1/products")
                .header("Authorization", securityToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Content-Type", CONTENT_TYPE))
                .andExpect(jsonPath("$.data.type").value("product"))
                .andExpect(jsonPath("$.data.id").exists())
                .andExpect(jsonPath("$.data.attributes.name").value("Test Product"))
                .andExpect(jsonPath("$.data.attributes.price").value(99.99));
    }

    @Test
    void createProduct_InvalidData() throws Exception {

        ProductRequest request = new ProductRequest(); // Sin datos obligatorios


        mockMvc.perform(post("/v1/products")
                .header("Authorization", securityToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getAllProducts_Success() throws Exception {

        ProductDto product1 = createMockProductDto();
        ProductDto product2 = createMockProductDto();
        List<ProductDto> products = Arrays.asList(product1, product2);
        Page<ProductDto> page = new PageImpl<>(products, PageRequest.of(0, 10), 2);

        when(productService.getAllProduct(any())).thenReturn(page);


        mockMvc.perform(get("/v1/products")
                .header("Authorization", securityToken)
                .accept(MediaType.parseMediaType(CONTENT_TYPE)))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", CONTENT_TYPE))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(2))
                .andExpect(jsonPath("$.data[0].type").value("product"))
                .andExpect(jsonPath("$.data[0].id").exists())
                .andExpect(jsonPath("$.data[0].attributes.name").exists())
                .andExpect(jsonPath("$.data[0].attributes.price").exists())
                .andExpect(jsonPath("$.meta.totalElements").value(2))
                .andExpect(jsonPath("$.meta.totalPages").value(1))
                .andExpect(jsonPath("$.meta.number").value(0))
                .andExpect(jsonPath("$.meta.size").value(10));
    }

    @Test
    void getProductById_Success() throws Exception {

        ProductDto mockResponse = createMockProductDto();
        when(productService.getProductById(any())).thenReturn(mockResponse);


        mockMvc.perform(get("/v1/products/" + mockResponse.getUuid())
                .header("Authorization", securityToken)
                .accept(MediaType.parseMediaType(CONTENT_TYPE)))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", CONTENT_TYPE))
                .andExpect(jsonPath("$.data.type").value("product"))
                .andExpect(jsonPath("$.data.id").exists())
                .andExpect(jsonPath("$.data.attributes.name").value("Test Product"))
                .andExpect(jsonPath("$.data.attributes.price").value(99.99));
    }

    @Test
    void getProductById_NotFound() throws Exception {

        when(productService.getProductById(any())).thenThrow(new RuntimeException("Product not found"));

        mockMvc.perform(get("/v1/products/" + UUID.randomUUID())
                .header("Authorization", securityToken)
                .accept(MediaType.parseMediaType(CONTENT_TYPE)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateProduct_Success() throws Exception {
        ProductRequest request = new ProductRequest();
        request.setName("Updated Product");
        request.setPrice(new BigDecimal("149.99"));

        ProductDto mockResponse = createMockProductDto();
        mockResponse.setName("Updated Product");
        mockResponse.setPrice(new BigDecimal("149.99"));

        when(productService.updateProduct(any(), any())).thenReturn(mockResponse);


        mockMvc.perform(patch("/v1/products/" + UUID.randomUUID())
                .header("Authorization", securityToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", CONTENT_TYPE))
                .andExpect(jsonPath("$.data.type").value("product"))
                .andExpect(jsonPath("$.data.id").exists())
                .andExpect(jsonPath("$.data.attributes.name").value("Updated Product"))
                .andExpect(jsonPath("$.data.attributes.price").value(149.99));
    }

    @Test
    void deleteProduct_Success() throws Exception {

        when(productService.deleteClient(any())).thenReturn(true);


        mockMvc.perform(delete("/v1/products/" + UUID.randomUUID())
                .header("Authorization", securityToken))
                .andExpect(status().isNoContent());
    }

    @Test
    void deleteProduct_NotFound() throws Exception {
        when(productService.deleteClient(any())).thenReturn(false);

        mockMvc.perform(delete("/v1/products/" + UUID.randomUUID())
                .header("Authorization", securityToken))
                .andExpect(status().isNotFound());
    }
}