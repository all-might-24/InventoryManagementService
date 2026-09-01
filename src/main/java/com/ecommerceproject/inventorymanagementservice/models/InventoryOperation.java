package com.ecommerceproject.inventorymanagementservice.models;

import com.ecommerceproject.inventorymanagementservice.models.enums.InventoryOperationType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
@Entity
@Table(
        name = "inventory_operations",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = "operation_id")
        }
)
public class InventoryOperation extends BaseEntity {

    @Column(name = "operation_id", nullable = false, unique = true)
    private String operationId;

    @Column(nullable = false)
    private Long productId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private InventoryOperationType operationType;

    @Column(nullable = false)
    private Integer quantity;
}