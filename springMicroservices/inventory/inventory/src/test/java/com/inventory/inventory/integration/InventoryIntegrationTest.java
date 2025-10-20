package com.inventory.inventory.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.jasminb.jsonapi.JSONAPIDocument;
import com.github.jasminb.jsonapi.Link;
import com.github.jasminb.jsonapi.Links;
import com.github.jasminb.jsonapi.ResourceConverter;
import com.inventory.inventory.config.TestConfig;
import com.inventory.inventory.config.TestJsonApiConfig;
import com.inventory.inventory.dto.abstracts.BaseDto;
import com.inventory.inventory.dto.abstracts.MetaData;
import com.inventory.inventory.dto.inventory.InventoryDto;
import com.inventory.inventory.dto.inventory.InventoryRequest;
import com.inventory.inventory.dto.product.ProductDto;
import com.inventory.inventory.dto.purchase.PurchaseDto;
import com.inventory.inventory.model.Inventory;
import com.inventory.inventory.repository.InventoryRepository;
import com.inventory.inventory.utils.TestProductData;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import({TestConfig.class, TestJsonApiConfig.class})
public class InventoryIntegrationTest {

    private static final String JSONAPI_CONTENT_TYPE = "application/vnd.api+json";
    private static final String JSON_CONTENT_TYPE = "application/json";
    private static final String AUTH_TOKEN = "Xf9G7qP3vL2R8kZs4nM1bYwQ6aT0jH5eC8dV3rL9pF2uN7xK1mBzA4sQ6yW0tE3o";
    private static MockWebServer mockWebServer;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ResourceConverter resourceConverter;

    @Autowired
    private InventoryRepository inventoryRepository;

    @BeforeAll
    static void setUp() throws IOException {
        mockWebServer = new MockWebServer();
        mockWebServer.start(8080);
    }

    @AfterAll
    static void tearDown() throws IOException {
        mockWebServer.shutdown();
    }

    @BeforeEach
    void init() {
        inventoryRepository.deleteAll();
        mockWebServer.getDispatcher().peek();
    }

    private String convertToJsonApi(Object obj) throws Exception {
        if (obj instanceof InventoryRequest) {
            // Para InventoryRequest, usar JSON plano (no JSON:API)
            return objectMapper.writeValueAsString(obj);
        } else if (obj instanceof PurchaseDto) {
            // Para PurchaseDto, usar JSON plano (no JSON:API)
            return objectMapper.writeValueAsString(obj);
        } else if (obj instanceof BaseDto) {
            BaseDto dto = (BaseDto) obj;
            if (dto.getUuid() == null) {
                dto.setUuid(UUID.randomUUID().toString());
            }
            setupLinksAndMeta(dto);
            JSONAPIDocument<?> document = new JSONAPIDocument<>(dto);
            byte[] bytes = resourceConverter.writeDocument(document);
            return new String(bytes);
        } else {
            throw new IllegalArgumentException("Unsupported type for conversion: " + obj.getClass());
        }
    }

    private void setupLinksAndMeta(BaseDto dto) {
        Links links = new Links();
        if (dto instanceof InventoryDto) {
            links.addLink("self", new Link("http://localhost:8081/v1/inventory/" + dto.getUuid()));
        } else if (dto instanceof ProductDto) {
            links.addLink("self", new Link("http://localhost:8081/v1/products/" + dto.getUuid()));
        }
        dto.setLinks(links);

        MetaData metaData = new MetaData();
        metaData.setCreatedDate(LocalDateTime.now());
        metaData.setLastModifiedDate(LocalDateTime.now());
        dto.setMetaData(metaData);
    }

    private InventoryRequest createInventoryRequest(UUID productId, Integer quantity) {
        InventoryRequest request = new InventoryRequest();
        request.setProductId(productId);
        request.setQuantity(quantity);
        return request;
    }

    private PurchaseDto createPurchaseRequest(Integer quantity) {
        PurchaseDto request = new PurchaseDto();
        request.setQuantitySold(quantity);
        return request;
    }

    private org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder addAuthHeader(
            org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder builder) {
        return builder.header("Authorization", "Bearer " + AUTH_TOKEN);
    }

    private static class JsonApiRequestWrapper {
        private final Object data;

        public JsonApiRequestWrapper(InventoryRequest request) {
            this.data = new Data(request);
        }

        private static class Data {
            private final String type = "inventory";
            private final Map<String, Object> attributes;

            public Data(InventoryRequest request) {
                this.attributes = new HashMap<>();
                attributes.put("product_id", request.getProductId().toString());
                attributes.put("quantity", request.getQuantity());
            }

            public String getType() {
                return type;
            }

            public Map<String, Object> getAttributes() {
                return attributes;
            }
        }

        public Object getData() {
            return data;
        }
    }


    private static class JsonApiPurchaseWrapper {
        private final Object data;

        public JsonApiPurchaseWrapper(PurchaseDto request) {
            this.data = new Data(request);
        }

        private static class Data {
            private final String type = "purchase";
            private final Map<String, Object> attributes;

            public Data(PurchaseDto request) {
                this.attributes = new HashMap<>();
                attributes.put("quantity_sold", request.getQuantitySold());
            }

            public String getType() {
                return type;
            }

            public Map<String, Object> getAttributes() {
                return attributes;
            }
        }

        public Object getData() {
            return data;
        }
    }

    @Test
    public void createInventory_Success() throws Exception {
        UUID productId = TestProductData.PRODUCT_1;
        InventoryRequest request = createInventoryRequest(productId, 100);

        ProductDto productDto = new ProductDto();
        productDto.setUuid(productId.toString());
        productDto.setName("Test Product");
        
        mockProductServiceResponse(productId, 200, productDto);

        mockMvc.perform(post("/v1/inventory")
                .contentType(JSON_CONTENT_TYPE)
                .accept(JSONAPI_CONTENT_TYPE)
                .header("Authorization", "Bearer " + AUTH_TOKEN)
                .content(convertToJsonApi(request)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(header().string("Content-Type", JSONAPI_CONTENT_TYPE))
                .andExpect(jsonPath("$.data.type").value("inventory"))
                .andExpect(jsonPath("$.data.attributes.productId").value(productId.toString()))
                .andExpect(jsonPath("$.data.attributes.quantity").value(100));
    }

    @Test
    public void createInventory_ProductServiceUnavailable() throws Exception {
        UUID productId = TestProductData.PRODUCT_2;
        InventoryRequest request = createInventoryRequest(productId, 100);

        // Encolar respuesta de error para el ProductClient
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(503)
                .setHeader("Content-Type", JSONAPI_CONTENT_TYPE)
                .setBody("{\"error\":\"Service Unavailable\"}"));

        mockMvc.perform(addAuthHeader(post("/v1/inventory"))
                .contentType(JSON_CONTENT_TYPE)
                .accept(JSONAPI_CONTENT_TYPE)
                .content(convertToJsonApi(request)))
                .andExpect(status().isBadRequest());

        assertThat(inventoryRepository.findByProductId(productId)).isEmpty();
    }

    @Test
    public void createInventory_ProductNotFound() throws Exception {
        UUID productId = TestProductData.PRODUCT_3;
        InventoryRequest request = createInventoryRequest(productId, 100);

        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(404)
                .setHeader("Content-Type", JSONAPI_CONTENT_TYPE)
                .setBody("{\"error\":\"Product not found\"}"));

        mockMvc.perform(addAuthHeader(post("/v1/inventory"))
                .contentType(JSON_CONTENT_TYPE)
                .accept(JSONAPI_CONTENT_TYPE)
                .content(convertToJsonApi(request)))
                .andExpect(status().isBadRequest());

        assertThat(inventoryRepository.findByProductId(productId)).isEmpty();
    }

    @Test
    public void processPurchase_Success() throws Exception {
        UUID productId = TestProductData.PRODUCT_4;
        
        // Create initial inventory
        ProductDto productDto = new ProductDto();
        productDto.setUuid(productId.toString());
        productDto.setName("Test Product");
        mockProductServiceResponse(productId, 200, productDto);
        
        createTestInventory(productId, 100);

        // Process purchase
        mockProductServiceResponse(productId, 200, productDto);
        PurchaseDto purchaseRequest = createPurchaseRequest(50);

        mockMvc.perform(addAuthHeader(patch("/v1/inventory/" + productId + "/purchase"))
                .contentType(JSON_CONTENT_TYPE)
                .accept(JSONAPI_CONTENT_TYPE)
                .content(convertToJsonApi(purchaseRequest)))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", JSONAPI_CONTENT_TYPE))
                .andExpect(jsonPath("$.data.type").value("inventory"))
                .andExpect(jsonPath("$.data.attributes.productId").value(productId.toString()))
                .andExpect(jsonPath("$.data.attributes.quantity").value(50));

        Inventory updatedInventory = inventoryRepository.findByProductId(productId).orElseThrow();
        assertThat(updatedInventory.getQuantity()).isEqualTo(50);
    }

    @Test
    public void processPurchase_InsufficientStock() throws Exception {
        UUID productId = TestProductData.PRODUCT_5;
        
        // Create initial inventory
        ProductDto productDto = new ProductDto();
        productDto.setUuid(productId.toString());
        productDto.setName("Test Product");
        mockProductServiceResponse(productId, 200, productDto);
        
        createTestInventory(productId, 100);

        // Try to purchase more than available
        mockProductServiceResponse(productId, 200, productDto);
        PurchaseDto purchaseRequest = createPurchaseRequest(150);

        mockMvc.perform(addAuthHeader(patch("/v1/inventory/" + productId + "/purchase"))
                .contentType(JSON_CONTENT_TYPE)
                .accept(JSONAPI_CONTENT_TYPE)
                .content(convertToJsonApi(purchaseRequest)))
                .andExpect(status().isBadRequest());

        Inventory inventory = inventoryRepository.findByProductId(productId).orElseThrow();
        assertThat(inventory.getQuantity()).isEqualTo(100);
    }

    @Test
    public void processPurchase_ProductServiceError() throws Exception {
        UUID productId = TestProductData.PRODUCT_6;
        
        // Create initial inventory
        ProductDto productDto = new ProductDto();
        productDto.setUuid(productId.toString());
        productDto.setName("Test Product");
        mockProductServiceResponse(productId, 200, productDto);
        
        createTestInventory(productId, 100);

        // Process purchase with service error
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(503)
                .setHeader("Content-Type", JSONAPI_CONTENT_TYPE));
                
        PurchaseDto purchaseRequest = createPurchaseRequest(50);

        mockMvc.perform(addAuthHeader(patch("/v1/inventory/" + productId + "/purchase"))
                .contentType(JSON_CONTENT_TYPE)
                .accept(JSONAPI_CONTENT_TYPE)
                .content(convertToJsonApi(purchaseRequest)))
                .andExpect(status().isInternalServerError());

        Inventory inventory = inventoryRepository.findByProductId(productId).orElseThrow();
        assertThat(inventory.getQuantity()).isEqualTo(100);
    }

    private MvcResult createTestInventory(UUID productId, Integer quantity) throws Exception {
        InventoryRequest request = createInventoryRequest(productId, quantity);
        return mockMvc.perform(addAuthHeader(post("/v1/inventory"))
                .contentType(JSON_CONTENT_TYPE)
                .accept(JSONAPI_CONTENT_TYPE)
                .content(convertToJsonApi(request)))
                .andExpect(status().isCreated())
                .andReturn();
    }

    private void mockProductServiceResponse(UUID productId, int status, ProductDto productDto) throws Exception {
        if (productDto.getUuid() == null) {
            productDto.setUuid(productId.toString());
        }
        setupLinksAndMeta(productDto);
        
        JSONAPIDocument<ProductDto> document = new JSONAPIDocument<>(productDto);
        byte[] response = resourceConverter.writeDocument(document);

        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(status)
                .setHeader("Content-Type", JSONAPI_CONTENT_TYPE)
                .setBody(new String(response)));
    }
}