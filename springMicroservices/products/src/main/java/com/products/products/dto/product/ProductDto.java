package com.products.products.dto.product;

import com.github.jasminb.jsonapi.annotations.Type;
import com.products.products.dto.abstracts.BaseDto;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Type("product")
@Getter
@Setter
public class ProductDto extends BaseDto {
    
    private String name;
    private BigDecimal price;
}
