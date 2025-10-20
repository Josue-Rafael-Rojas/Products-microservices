package com.inventory.inventory.dto.inventory;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class InventoryRequest {
     @NotNull(message = "Product ID is required")
     private UUID productId;

     @NotNull(message = "Quantity is required")
     @Min(value = 1, message = "Quantity must be at least 1")
     private Integer quantity;
}
