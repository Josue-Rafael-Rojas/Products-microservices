package com.products.products.controller;

import com.github.jasminb.jsonapi.JSONAPIDocument;
import com.github.jasminb.jsonapi.ResourceConverter;
import com.products.products.dto.product.ProductDto;
import com.products.products.dto.product.ProductRequest;
import com.products.products.exception.common.ErrorDto;
import com.products.products.service.product.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
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

    @Operation(
            summary = "Listar todos los productos",
            description = "Obtiene una lista paginada de todos los productos disponibles en el sistema.",
            parameters = {
                    @Parameter(
                            name = "page",
                            description = "Número de página (empieza en 0)",
                            schema = @Schema(type = "integer", defaultValue = "0")
                    ),
                    @Parameter(
                            name = "size",
                            description = "Cantidad de elementos por página",
                            schema = @Schema(type = "integer", defaultValue = "10")
                    )
            },
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Lista de productos obtenida exitosamente",
                            content = @Content(
                                    mediaType = "application/vnd.api+json",
                                    schema = @Schema(implementation = ProductDto.class)
                            )
                    )
            }
    )
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

    @Operation(
            summary = "Obtener un producto por ID",
            description = "Obtiene la información detallada de un producto específico usando su UUID.",
            parameters = {
                    @Parameter(
                            name = "uuid",
                            description = "UUID del producto a consultar",
                            required = true,
                            schema = @Schema(type = "string", format = "uuid")
                    )
            },
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Producto encontrado exitosamente",
                            content = @Content(
                                    mediaType = "application/vnd.api+json",
                                    schema = @Schema(implementation = ProductDto.class)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "Producto no encontrado",
                            content = @Content(
                                    mediaType = "application/vnd.api+json",
                                    schema = @Schema(implementation = ErrorDto.class)
                            )
                    )
            }
    )
    @GetMapping(
            value = "/{uuid}",
            produces = "application/vnd.api+json"
    )
    public ResponseEntity<byte[]> getProductById(@PathVariable String uuid) {
        ProductDto product = productService.getProductById(uuid);
        try {

            JSONAPIDocument<ProductDto> responseDoc = new JSONAPIDocument<>(product);
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

    @Operation(
            summary = "Actualizar un producto",
            description = "Actualiza la información de un producto existente. Permite actualizar nombre y/o precio.",
            parameters = {
                    @Parameter(
                            name = "uuid",
                            description = "UUID del producto a actualizar",
                            required = true,
                            schema = @Schema(type = "string", format = "uuid")
                    )
            },
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Datos del producto a actualizar. Se pueden actualizar name y/o price",
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ProductRequest.class)
                    )
            ),
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Producto actualizado exitosamente",
                            content = @Content(
                                    mediaType = "application/vnd.api+json",
                                    schema = @Schema(implementation = ProductDto.class)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "Producto no encontrado",
                            content = @Content(
                                    mediaType = "application/vnd.api+json",
                                    schema = @Schema(implementation = ErrorDto.class)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "Datos inválidos",
                            content = @Content(
                                    mediaType = "application/vnd.api+json",
                                    schema = @Schema(implementation = ErrorDto.class)
                            )
                    )
            }
    )
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

    @Operation(
            summary = "Eliminar un producto",
            description = "Elimina un producto existente del sistema usando su UUID.",
            parameters = {
                    @Parameter(
                            name = "uuid",
                            description = "UUID del producto a eliminar",
                            required = true,
                            schema = @Schema(type = "string", format = "uuid")
                    )
            },
            responses = {
                    @ApiResponse(
                            responseCode = "204",
                            description = "Producto eliminado exitosamente"
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "Producto no encontrado",
                            content = @Content(
                                    mediaType = "application/vnd.api+json",
                                    schema = @Schema(implementation = ErrorDto.class)
                            )
                    )
            }
    )
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
