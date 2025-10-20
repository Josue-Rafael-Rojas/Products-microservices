package com.products.products.service.product;

import com.products.products.dto.product.ProductDto;
import com.products.products.dto.product.ProductRequest;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ProductService {
    ProductDto createProduct(ProductRequest productDto);
    ProductDto getProductById(String uuidProduct);
    Page<ProductDto> getAllProduct(Pageable pageable);
    ProductDto updateProduct(String  uuidProduct, ProductRequest productDto);
    Boolean deleteClient(String  uuidProduct);
}
