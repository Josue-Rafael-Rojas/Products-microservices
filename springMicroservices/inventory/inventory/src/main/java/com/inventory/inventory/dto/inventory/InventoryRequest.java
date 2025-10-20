package com.inventory.inventory.dto.inventory;

import jakarta.validation.constraints.NotEmpty;
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
     @NotEmpty(message = "ProductId is required")
     private UUID productId;

     @NotEmpty(message = "Quantity is required")
     private Integer quantity;
}
