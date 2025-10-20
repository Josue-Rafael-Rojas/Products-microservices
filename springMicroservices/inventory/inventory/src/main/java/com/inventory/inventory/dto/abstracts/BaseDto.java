package com.inventory.inventory.dto.abstracts;

import com.github.jasminb.jsonapi.annotations.Id;
import com.github.jasminb.jsonapi.annotations.Links;
import com.github.jasminb.jsonapi.annotations.Meta;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class BaseDto {
    @Id
    private String uuid;

    @Meta
    private MetaData metaData;

    @Links
    private com.github.jasminb.jsonapi.Links links;

}
