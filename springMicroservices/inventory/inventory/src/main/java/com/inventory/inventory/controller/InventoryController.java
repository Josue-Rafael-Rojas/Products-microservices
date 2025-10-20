package com.inventory.inventory.controller;

import com.github.jasminb.jsonapi.JSONAPIDocument;
import com.github.jasminb.jsonapi.ResourceConverter;
import com.github.jasminb.jsonapi.exceptions.DocumentSerializationException;
import com.inventory.inventory.dto.inventory.InventoryDto;
import com.inventory.inventory.dto.inventory.InventoryRequest;
import com.inventory.inventory.dto.purchase.PurchaseDto;
import com.inventory.inventory.exception.common.ErrorDto;
import com.inventory.inventory.service.inventory.InventoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/inventory")
@AllArgsConstructor
@Tag(name = "Inventory Controller", description = "API para gestionar el inventario de productos")
public class InventoryController {

    private final InventoryService inventoryService;
    private final ResourceConverter resourceConverter;


    @Operation(
            summary = "Crear un nuevo registro de inventario",
            description = "Esta operación permite crear un nuevo registro de inventario para un producto específico, " +
                    "estableciendo su cantidad inicial. También obtiene y vincula la información del producto desde el microservicio de productos.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Datos del inventario a crear. Se requiere el ID del producto y la cantidad inicial",
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = InventoryRequest.class)
                    )
            ),
            responses = {
                    @ApiResponse(
                            responseCode = "201",
                            description = "Inventario creado exitosamente",
                            content = @Content(
                                    mediaType = "application/vnd.api+json",
                                    schema = @Schema(implementation = InventoryDto.class)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "Datos inválidos o producto no encontrado",
                            content = @Content(
                                    mediaType = "application/vnd.api+json",
                                    schema = @Schema(implementation = ErrorDto.class)
                            )
                    )
            }
    )
    @PostMapping(
            consumes = "application/json",
            produces = "application/vnd.api+json"
    )
    public ResponseEntity<byte[]> createClient(@RequestBody @Valid InventoryRequest inventoryRequest) {
        try {
            // 1. Service retorna ProductDto
            InventoryDto createdInventory = inventoryService.createInventory(inventoryRequest);

            // 2. Serializar ProductDto → JSON:API
            JSONAPIDocument<InventoryDto> responseDoc = new JSONAPIDocument<>(createdInventory);

            byte[] jsonApiResponse = resourceConverter.writeDocument(responseDoc);

            // 3. Retornar
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
            summary = "Consultar inventario de un producto",
            description = "Obtiene la información del inventario para un producto específico, incluyendo su cantidad disponible " +
                    "y la información completa del producto obtenida del microservicio de productos.",
            parameters = {
                    @Parameter(
                            name = "productId",
                            description = "UUID del producto a consultar",
                            required = true,
                            schema = @Schema(type = "string", format = "uuid")
                    )
            },
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Inventario encontrado exitosamente",
                            content = @Content(
                                    mediaType = "application/vnd.api+json",
                                    schema = @Schema(implementation = InventoryDto.class)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "Inventario no encontrado para el producto especificado",
                            content = @Content(
                                    mediaType = "application/vnd.api+json",
                                    schema = @Schema(implementation = ErrorDto.class)
                            )
                    )
            }
    )
    @GetMapping(
            value = "/{productId}",
            produces = "application/vnd.api+json"
    )
    public ResponseEntity<byte[]> getInventoryWithProduct(@PathVariable String productId) {
        try {
            InventoryDto inventory = inventoryService.getInventoryWithProduct(productId);

            // El ResourceConverter manejará automáticamente la sección included
            JSONAPIDocument<InventoryDto> responseDoc = new JSONAPIDocument<>(inventory);
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
            summary = "Registrar una compra y actualizar inventario",
            description = "Procesa una compra actualizando la cantidad disponible en el inventario. " +
                    "Valida que haya suficiente stock y emite un evento cuando el inventario cambia.",
            parameters = {
                    @Parameter(
                            name = "productId",
                            description = "UUID del producto a comprar",
                            required = true,
                            schema = @Schema(type = "string", format = "uuid")
                    )
            },
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Datos de la compra. Se requiere la cantidad a comprar",
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = PurchaseDto.class)
                    )
            ),
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Compra procesada exitosamente",
                            content = @Content(
                                    mediaType = "application/vnd.api+json",
                                    schema = @Schema(implementation = InventoryDto.class)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "Datos inválidos o stock insuficiente",
                            content = @Content(
                                    mediaType = "application/vnd.api+json",
                                    schema = @Schema(implementation = ErrorDto.class)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "Producto no encontrado en inventario",
                            content = @Content(
                                    mediaType = "application/vnd.api+json",
                                    schema = @Schema(implementation = ErrorDto.class)
                            )
                    )
            }
    )
    @PatchMapping(
            value = "/{productId}/purchase",
            consumes = "application/json",
            produces = "application/vnd.api+json"
    )
    public ResponseEntity<byte[]> processPurchase(
            @PathVariable String productId,
            @RequestBody @Valid PurchaseDto purchaseDto) {
        InventoryDto updatedInventory = inventoryService.processPurchase(productId, purchaseDto);
        try {
            JSONAPIDocument<InventoryDto> responseDoc = new JSONAPIDocument<>(updatedInventory);
            byte[] jsonApiResponse = resourceConverter.writeDocument(responseDoc);

            return ResponseEntity
                    .ok()
                    .header("Content-Type", "application/vnd.api+json")
                    .body(jsonApiResponse);
        } catch (DocumentSerializationException e) { // Solo capturar excepciones específicas
            e.printStackTrace();
            return ResponseEntity.badRequest().build();
        }
    }

}
