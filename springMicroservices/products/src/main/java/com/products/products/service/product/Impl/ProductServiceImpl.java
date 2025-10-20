package com.products.products.service.product.Impl;

import com.products.products.dto.product.ProductDto;
import com.products.products.dto.product.ProductMapper;
import com.products.products.dto.product.ProductRequest;
import com.products.products.exception.ProductNotFoundException;
import com.products.products.exception.UuidInvalidException;
import com.products.products.model.Product;
import com.products.products.repository.ProductRepository;
import com.products.products.service.product.ProductService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

@Service
public class ProductServiceImpl implements ProductService {
    
    private static final Logger logger = LoggerFactory.getLogger(ProductServiceImpl.class);

    private final ProductMapper productMapper;
    private final ProductRepository productRepository;


    public ProductServiceImpl(ProductMapper productMapper, ProductRepository productRepository) {
        this.productMapper = productMapper;
        this.productRepository = productRepository;
    }


    @Override
    public ProductDto createProduct(ProductRequest productRequest) {
       logger.info("Creating new product with name: {}", productRequest.getName());
       Product product = productMapper.toModel(productRequest);
       Product productResponse = productRepository.save(product);
       logger.info("Product created successfully with ID: {}", productResponse.getUuid());
       return productMapper.toDto(productResponse);
    }

    @Override
    public ProductDto getProductById(String uuidProduct) {
        logger.info("Fetching product with ID: {}", uuidProduct);
        UUID uuid;
        try {
            uuid = UUID.fromString(uuidProduct);
        } catch (IllegalArgumentException e) {
            logger.error("Invalid UUID format: {}", uuidProduct);
            throw new UuidInvalidException(uuidProduct);
        }
        Product product = productRepository.findById(uuid).orElseThrow(() -> {
            logger.error("Product not found with ID: {}", uuid);
            return new ProductNotFoundException(uuid);
        });

        logger.info("Product found successfully: {}", product.getName());
        return productMapper.toDto(product);
    }

    @Override
    public Page<ProductDto> getAllProduct(Pageable pageable) {
        logger.info("Fetching all products with page: {} and size: {}", pageable.getPageNumber(), pageable.getPageSize());
        Page<ProductDto> result = productRepository.findAll(pageable)
                .map(productMapper::toDto);
        logger.info("Found {} products in page {} of {}", 
            result.getNumberOfElements(), 
            result.getNumber() + 1, 
            result.getTotalPages());
        return result;
    }

    @Override
    public ProductDto updateProduct(String uuidProduct, ProductRequest productRequest) {
        logger.info("Updating product with ID: {} and new name: {}", uuidProduct, productRequest.getName());
        UUID uuid;
        try {
            uuid = UUID.fromString(uuidProduct);
        } catch (IllegalArgumentException e) {
            logger.error("Invalid UUID format for update: {}", uuidProduct);
            throw new UuidInvalidException(uuidProduct);
        }

        Product product = productRepository.findById(uuid).orElseThrow(() -> {
            logger.error("Product not found for update with ID: {}", uuid);
            return new ProductNotFoundException(uuid);
        });
        
        logger.info("Found product to update: {}", product.getName());
        productMapper.updateModel(productRequest, product);
        Product updatedProduct = productRepository.save(product);
        logger.info("Product updated successfully: {}", updatedProduct.getName());

        return productMapper.toDto(updatedProduct);
    }

    @Override
    public Boolean deleteClient(String uuidProduct) {
        logger.info("Attempting to delete product with ID: {}", uuidProduct);
        UUID uuid;
        try {
            uuid = UUID.fromString(uuidProduct);
        } catch (IllegalArgumentException e) {
            logger.error("Invalid UUID format for deletion: {}", uuidProduct);
            throw new UuidInvalidException(uuidProduct);
        }

        try {
            productRepository.deleteById(uuid);
            logger.info("Product deleted successfully with ID: {}", uuid);
            return true;
        } catch (Exception ex) {
            logger.error("Error deleting product with ID: {}. Error: {}", uuid, ex.getMessage());
            return false;
        }
    }
}
