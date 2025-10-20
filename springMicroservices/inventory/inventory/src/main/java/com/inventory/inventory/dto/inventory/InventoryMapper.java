package com.inventory.inventory.dto.inventory;

import com.github.jasminb.jsonapi.Link;
import com.github.jasminb.jsonapi.Links;
import com.inventory.inventory.dto.abstracts.MetaData;
import com.inventory.inventory.model.Inventory;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
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
public interface InventoryMapper {

    InventoryDto toDto(Inventory inventory);

    Inventory toModel(InventoryRequest inventoryRequest);

    void updateModel(InventoryRequest inventoryRequest, @MappingTarget Inventory inventory);

    @AfterMapping
    default void mapMetaData(Inventory inventory, @MappingTarget InventoryDto inventoryDto) {
        if (inventory != null) {
            MetaData metaData = new MetaData();
            metaData.setCreatedDate(inventory.getCreatedDate());
            metaData.setLastModifiedDate(inventory.getLastModifiedDate());
            inventoryDto.setMetaData(metaData);
        }
    }

    @AfterMapping
    default void mapLinks(Inventory inventory, @MappingTarget InventoryDto inventoryDto) {
        if (inventory != null && inventory.getUuid() != null) {
            Links resourceLinks = new Links();
            String selfUrl = "http://localhost:8081/v1/inventory/" + inventory.getUuid();
            resourceLinks.addLink("self", new Link(selfUrl));
            inventoryDto.setLinks(resourceLinks);


            if (inventoryDto.getProduct() != null) {
                Links productLinks = new Links();
                String productSelfUrl = "http://localhost:8081/v1/products/" + inventory.getProductId();
                productLinks.addLink("self", new Link(productSelfUrl));
                inventoryDto.getProduct().setLinks(productLinks);
            }
        }
    }
}
