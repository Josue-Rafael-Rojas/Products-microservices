package com.products.products.service.Impl;

import com.products.products.dto.product.ProductDto;
import com.products.products.dto.product.ProductMapper;
import com.products.products.dto.product.ProductRequest;
import com.products.products.exception.ProductNotFoundException;
import com.products.products.exception.UuidInvalidException;
import com.products.products.model.Product;
import com.products.products.repository.ProductRepository;
import com.products.products.service.ProductService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class ProductServiceImpl implements ProductService {

    private final ProductMapper productMapper;
    private final ProductRepository productRepository;


    public ProductServiceImpl(ProductMapper productMapper, ProductRepository productRepository) {
        this.productMapper = productMapper;
        this.productRepository = productRepository;
    }


    @Override
    public ProductDto createProduct(ProductRequest productRequest) {
       Product product = productMapper.toModel(productRequest);
       Product productResponse = productRepository.save(product);
       return productMapper.toDto(productResponse);
    }

    @Override
    public ProductDto getProductById(String uuidProduct) {
        UUID uuid;
        try {
            uuid = UUID.fromString(uuidProduct);
        } catch (IllegalArgumentException e) {
            throw new UuidInvalidException(uuidProduct);
        }
        Product product = productRepository.findById(uuid).orElseThrow(() -> new ProductNotFoundException(uuid));

        return productMapper.toDto(product);
    }

    @Override
    public Page<ProductDto> getAllProduct(Pageable pageable) {
        return productRepository.findAll(pageable)
                .map(productMapper::toDto);
    }

    @Override
    public ProductDto updateProduct(String uuidProduct, ProductRequest productRequest) {
        UUID uuid;
        try {
            uuid = UUID.fromString(uuidProduct);
        } catch (IllegalArgumentException e) {
            throw new UuidInvalidException(uuidProduct);
        }

        Product product = productRepository.findById(uuid).orElseThrow(() -> new ProductNotFoundException(uuid));
        productMapper.updateModel(productRequest, product);

        return productMapper.toDto(productRepository.save(product));
    }

    @Override
    public Boolean deleteClient(String uuidProduct) {
        UUID uuid;
        try {
            uuid = UUID.fromString(uuidProduct);
        } catch (IllegalArgumentException e) {
            throw new UuidInvalidException(uuidProduct);
        }

        try {
            productRepository.deleteById(uuid);
            return true;
        }catch (Exception ex){
            ex.printStackTrace();
            return false;
        }
    }
}
