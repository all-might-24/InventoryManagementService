package com.ecommerceproject.inventorymanagementservice.repositories;

import com.ecommerceproject.inventorymanagementservice.models.Inventory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface InventoryRepository extends JpaRepository<Inventory, Long> {

    Optional<Inventory> findByProductId(Long productId);
}
