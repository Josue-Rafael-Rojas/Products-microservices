package com.products.products.controller;

import com.github.jasminb.jsonapi.JSONAPIDocument;
import com.github.jasminb.jsonapi.ResourceConverter;
import com.products.products.dto.product.ProductDto;
import com.products.products.dto.product.ProductRequest;
import com.products.products.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.persistence.Table;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.RequestParam;
import com.github.jasminb.jsonapi.Links;
import com.github.jasminb.jsonapi.Link;

import java.util.List;
import java.util.HashMap;

@RestController
@RequestMapping("/v1/products")
@AllArgsConstructor
@Tag(name = "Product", description = "Controlador para consumir los servicios de Product")
public class ProductController {

    private final ProductService productService;
    private final ResourceConverter resourceConverter;


    @PostMapping(
            consumes = "application/json",
            produces = "application/vnd.api+json"
    )
    @Operation(
            summary = "Crear un nuevo producto.",
            description = "Esta operación permite crear un nuevo producto y almacenarlo en la base de datos del sistema.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Para la creacion de un producto es requerido el name y el price estos dos son obligatorios",
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ProductRequest.class)
                    )
            ),
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Creacion exitosa",
                            content = @Content(
                                    mediaType = "application/vnd.api+json",
                                    schema = @Schema(implementation = ProductDto.class)
                            )
                    )
            }
    )
    public ResponseEntity<byte[]> createClient(@RequestBody @Valid ProductRequest productRequest) {
        try {
            ProductDto createdProduct = productService.createProduct(productRequest);

            JSONAPIDocument<ProductDto> responseDoc = new JSONAPIDocument<>(createdProduct);

            byte[] jsonApiResponse = resourceConverter.writeDocument(responseDoc);

            return ResponseEntity
                    .status(201)
                    .header("Content-Type", "application/vnd.api+json")
                    .body(jsonApiResponse);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping(produces = "application/vnd.api+json")
    public ResponseEntity<byte[]> getAllProducts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        Pageable pageable = PageRequest.of(page, size);
        Page<ProductDto> productPage = productService.getAllProduct(pageable);

        try {
            JSONAPIDocument<List<ProductDto>> responseDoc = new JSONAPIDocument<>(
                productPage.getContent()
            );

            HashMap<String, Object> metaMap = new HashMap<>();
            metaMap.put("totalElements", productPage.getTotalElements());
            metaMap.put("totalPages", productPage.getTotalPages());
            metaMap.put("number", productPage.getNumber());
            metaMap.put("size", productPage.getSize());
            responseDoc.setMeta(metaMap);

            Links links = new Links();
            String baseUrl = "/v1/products";

            links.addLink("self", 
                new Link(String.format("%s?page=%d&size=%d", 
                    baseUrl, page, size)));

            links.addLink("first", 
                new Link(String.format("%s?page=0&size=%d", 
                    baseUrl, size)));

            links.addLink("last", 
                new Link(String.format("%s?page=%d&size=%d", 
                    baseUrl, productPage.getTotalPages() - 1, size)));

            if (productPage.hasNext()) {
                links.addLink("next", 
                    new Link(String.format("%s?page=%d&size=%d", 
                        baseUrl, page + 1, size)));
            }

            if (productPage.hasPrevious()) {
                links.addLink("prev", 
                    new Link(String.format("%s?page=%d&size=%d", 
                        baseUrl, page - 1, size)));
            }
            
            responseDoc.setLinks(links);

            byte[] jsonApiResponse = resourceConverter.writeDocumentCollection(responseDoc);

            return ResponseEntity
                    .ok()
                    .header("Content-Type", "application/vnd.api+json")
                    .body(jsonApiResponse);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping(
            value = "/{uuid}",
            produces = "application/vnd.api+json"
    )
    public ResponseEntity<byte[]> getProductById(@PathVariable String uuid) {
        ProductDto product = productService.getProductById(uuid);
        try {
            // 2. Serializar ProductDto → JSON:API
            JSONAPIDocument<ProductDto> responseDoc = new JSONAPIDocument<>(product);
            byte[] jsonApiResponse = resourceConverter.writeDocument(responseDoc);

            // 3. Retornar
            return ResponseEntity
                    .ok()
                    .header("Content-Type", "application/vnd.api+json")
                    .body(jsonApiResponse);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().build();
        }
    }

    @PatchMapping(
            value = "/{uuid}",
            consumes = "application/json",
            produces = "application/vnd.api+json"
    )
    public ResponseEntity<byte[]> updateProduct(
            @PathVariable String uuid,
            @RequestBody ProductRequest productRequest) {
        try {
            ProductDto updatedProduct = productService.updateProduct(uuid, productRequest);

            JSONAPIDocument<ProductDto> responseDoc = new JSONAPIDocument<>(updatedProduct);
            byte[] jsonApiResponse = resourceConverter.writeDocument(responseDoc);

            return ResponseEntity
                    .ok()
                    .header("Content-Type", "application/vnd.api+json")
                    .body(jsonApiResponse);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().build();
        }
    }

    @DeleteMapping(
            value = "/{uuid}",
            produces = "application/vnd.api+json"
    )
    public ResponseEntity<Void> deleteProduct(@PathVariable String uuid) {
        try {
            Boolean deleted = productService.deleteClient(uuid);
            if (deleted) {
                return ResponseEntity.noContent().build();
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().build();
        }
    }


}
