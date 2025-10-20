package com.inventory.inventory.dto.inventory;

import com.github.jasminb.jsonapi.annotations.Relationship;
import com.github.jasminb.jsonapi.annotations.Type;
import com.inventory.inventory.dto.abstracts.BaseDto;
import com.inventory.inventory.dto.product.ProductDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.util.UUID;

@Type("inventory")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class InventoryDto extends BaseDto {

    private UUID productId;
    private Integer quantity;

    @Relationship("product")
    private ProductDto product;
}
