package com.products.products.service;

import com.products.products.dto.product.ProductDto;
import com.products.products.dto.product.ProductMapper;
import com.products.products.dto.product.ProductRequest;
import com.products.products.exception.ProductNotFoundException;
import com.products.products.exception.UuidInvalidException;
import com.products.products.model.Product;
import com.products.products.repository.ProductRepository;
import com.products.products.service.product.Impl.ProductServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private ProductMapper productMapper;

    @InjectMocks
    private ProductServiceImpl productService;

    private Product product;
    private ProductRequest productRequest;
    private UUID productId;

    @BeforeEach
    void setUp() {
        productId = UUID.randomUUID();
        
        product = new Product();
        product.setUuid(productId);
        product.setName("Test Product");
        product.setPrice(BigDecimal.valueOf(100.00));

        productRequest = new ProductRequest();
        productRequest.setName("Test Product");
        productRequest.setPrice(BigDecimal.valueOf(100.00));
    }

    @Test
    void createProduct_Success() {
        // Mock del mapper
        when(productMapper.toModel(productRequest)).thenReturn(product);
        when(productMapper.toDto(product)).thenReturn(createProductDto());
        when(productRepository.save(any(Product.class))).thenReturn(product);

        ProductDto result = productService.createProduct(productRequest);

        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo(productRequest.getName());
        assertThat(result.getPrice()).isEqualTo(productRequest.getPrice());
        
        verify(productRepository).save(any(Product.class));
        verify(productMapper).toModel(productRequest);
        verify(productMapper).toDto(product);
    }

    @Test
    void getProductById_Success() {
        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        when(productMapper.toDto(product)).thenReturn(createProductDto());

        ProductDto result = productService.getProductById(productId.toString());

        assertThat(result).isNotNull();
        assertThat(result.getUuid()).isEqualTo(productId.toString());
        assertThat(result.getName()).isEqualTo(product.getName());
        
        verify(productRepository).findById(productId);
        verify(productMapper).toDto(product);
    }

    @Test
    void getProductById_InvalidUUID_ThrowsException() {
        String invalidUuid = "invalid-uuid";

        assertThatThrownBy(() -> productService.getProductById(invalidUuid))
            .isInstanceOf(UuidInvalidException.class);
    }

    @Test
    void getProductById_NotFound_ThrowsException() {
        when(productRepository.findById(any(UUID.class))).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productService.getProductById(productId.toString()))
            .isInstanceOf(ProductNotFoundException.class);
    }

    @Test
    void getAllProducts_Success() {
        PageRequest pageRequest = PageRequest.of(0, 10);
        List<Product> products = List.of(product);
        Page<Product> productPage = new PageImpl<>(products, pageRequest, 1);

        when(productRepository.findAll(pageRequest)).thenReturn(productPage);
        when(productMapper.toDto(product)).thenReturn(createProductDto());

        Page<ProductDto> result = productService.getAllProduct(pageRequest);

        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getName()).isEqualTo(product.getName());
        assertThat(result.getTotalElements()).isEqualTo(1);
        
        verify(productRepository).findAll(pageRequest);
        verify(productMapper).toDto(product);
    }

    @Test
    void updateProduct_Success() {
        ProductRequest updateRequest = new ProductRequest();
        updateRequest.setName("Updated Product");
        updateRequest.setPrice(BigDecimal.valueOf(150.00));

        Product updatedProduct = new Product();
        updatedProduct.setUuid(productId);
        updatedProduct.setName(updateRequest.getName());
        updatedProduct.setPrice(updateRequest.getPrice());

        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        doAnswer(invocation -> {
            Product productToUpdate = invocation.getArgument(1);
            productToUpdate.setName(updateRequest.getName());
            productToUpdate.setPrice(updateRequest.getPrice());
            return null;
        }).when(productMapper).updateModel(eq(updateRequest), any(Product.class));
        
        when(productRepository.save(any(Product.class))).thenReturn(updatedProduct);
        when(productMapper.toDto(any(Product.class))).thenReturn(createUpdatedProductDto(updateRequest));

        ProductDto result = productService.updateProduct(productId.toString(), updateRequest);

        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo(updateRequest.getName());
        assertThat(result.getPrice()).isEqualTo(updateRequest.getPrice());
        
        verify(productRepository).findById(productId);
        verify(productMapper).updateModel(eq(updateRequest), any(Product.class));
        verify(productRepository).save(any(Product.class));
    }

    @Test
    void updateProduct_NotFound_ThrowsException() {
        when(productRepository.findById(any(UUID.class))).thenReturn(Optional.empty());

        assertThatThrownBy(() -> 
            productService.updateProduct(productId.toString(), new ProductRequest()))
            .isInstanceOf(ProductNotFoundException.class);
    }

    @Test
    void deleteProduct_Success() {
        doNothing().when(productRepository).deleteById(productId);

        boolean result = productService.deleteClient(productId.toString());

        assertThat(result).isTrue();
        verify(productRepository).deleteById(productId);
    }

    @Test
    void deleteProduct_WithException_ReturnsFalse() {
        doThrow(new RuntimeException("Error deleting product"))
            .when(productRepository).deleteById(any(UUID.class));

        boolean result = productService.deleteClient(productId.toString());

        assertThat(result).isFalse();
        verify(productRepository).deleteById(any(UUID.class));
    }

    @Test
    void deleteProduct_InvalidUUID_ThrowsException() {
        String invalidUuid = "invalid-uuid";

        assertThatThrownBy(() -> productService.deleteClient(invalidUuid))
            .isInstanceOf(UuidInvalidException.class);
    }

    private ProductDto createProductDto() {
        ProductDto dto = new ProductDto();
        dto.setUuid(productId.toString());
        dto.setName(product.getName());
        dto.setPrice(product.getPrice());
        return dto;
    }

    private ProductDto createUpdatedProductDto(ProductRequest request) {
        ProductDto dto = new ProductDto();
        dto.setUuid(productId.toString());
        dto.setName(request.getName());
        dto.setPrice(request.getPrice());
        return dto;
    }
}