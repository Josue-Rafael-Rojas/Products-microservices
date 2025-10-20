package com.inventory.inventory.dto.product;


import com.github.jasminb.jsonapi.annotations.Id;
import com.github.jasminb.jsonapi.Links;
import com.github.jasminb.jsonapi.annotations.Type;
import com.inventory.inventory.dto.abstracts.BaseDto;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Type("product")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ProductDto extends BaseDto {

    private String name;
    private BigDecimal price;


}