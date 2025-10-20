package com.products.products.dto.product;


import com.github.jasminb.jsonapi.Link;
import com.github.jasminb.jsonapi.Links;
import com.products.products.dto.abstracts.MetaData;
import com.products.products.model.Product;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValueCheckStrategy;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;


@Mapper(
        componentModel = "spring",
        nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
        unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface ProductMapper {

    ProductDto toDto(Product product);

    Product toModel(ProductRequest productRequest);

    void updateModel(ProductRequest productRequest, @MappingTarget Product product);

    @AfterMapping
    default void mapMetaData(Product product, @MappingTarget ProductDto productDto) {
        if (product != null) {
            MetaData metaData = new MetaData();
            metaData.setCreatedDate(product.getCreatedDate());
            metaData.setLastModifiedDate(product.getLastModifiedDate());
            productDto.setMetaData(metaData);
        }
    }

    @AfterMapping
    default void mapLinks(Product product, @MappingTarget ProductDto productDto) {
        if (product != null && product.getUuid() != null) {

            Links resourceLinks = new Links();

            String selfUrl = "http://localhost:8080/v1/products/" + product.getUuid();
            resourceLinks.addLink("self", new Link(selfUrl));

            productDto.setLinks(resourceLinks);
        }
    }
}
