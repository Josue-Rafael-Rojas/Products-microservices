package com.inventory.inventory.model.listeners;

import com.inventory.inventory.model.Inventory;
import com.inventory.inventory.repository.InventoryRepository;
import jakarta.persistence.PostLoad;
import jakarta.persistence.PostPersist;
import jakarta.persistence.PostUpdate;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component("listener.inventory.entity_listener")
public class InventoryListener {

    private static InventoryRepository inventoryRepository;
    private static final Logger log = LoggerFactory.getLogger(InventoryListener.class);
    int oldQuantity;

    @Autowired
    public void setInventoryRepository(InventoryRepository inventoryRepository) {
        InventoryListener.inventoryRepository = inventoryRepository;
    }

    @PostLoad
    public void onPostLoad(Inventory inventory) {
        oldQuantity = inventory.getQuantity();
        log.debug("Inventory PostLoad -> oldQuantity set to {}", inventory.getQuantity());
    }

    @PrePersist
    public void onPreInsert(Inventory inventory) {
        log.info("inventory PrePersist -> event: {}", inventory);
    }

    @PreUpdate
    public void onPreUpdate(Inventory inventory) {
        log.info("inventory PreUpdate -> event: {}", inventory);
    }

    @PostUpdate
    public void onPostUpdate(Inventory inventory) {
        int currentQuantity = inventory.getQuantity();
        int delta = currentQuantity - oldQuantity;
        if(delta != 0){
            if (delta < 0) {
                log.info("-----------Purchase Event--------------");
                log.info("Product ID: {}", inventory.getProductId());
                log.info("Units purchased: {}", Math.abs(delta));
                log.info("Remaining stock: {}", currentQuantity);
                if (currentQuantity <= 5) {
                    log.warn("Low stock alert! Only {} units remaining", currentQuantity);
                }
            } else {
                // Es una reposición de inventario
                log.info("-----------Restock Event--------------");
                log.info("Product ID: {}", inventory.getProductId());
                log.info("Units added: {}", delta);
                log.info("New stock level: {}", currentQuantity);
            }
        } else {
            log.info("-----------No inventory change--------------");
        }
    }

    @PostPersist
    public void onPostInsert(Inventory inventory) {
        log.info("inventory PostPersist -> event: {}", inventory);
    }
}
