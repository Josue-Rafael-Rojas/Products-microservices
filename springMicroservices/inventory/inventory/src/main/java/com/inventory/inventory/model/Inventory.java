package com.inventory.inventory.model;


import com.inventory.inventory.model.abstracts.BaseModel;
import com.inventory.inventory.model.listeners.InventoryListener;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.util.UUID;

@Entity
@Table(name = "inventories")
@EntityListeners({AuditingEntityListener.class, InventoryListener.class})
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class Inventory extends BaseModel {
    private UUID productId;
    private Integer quantity;
}
