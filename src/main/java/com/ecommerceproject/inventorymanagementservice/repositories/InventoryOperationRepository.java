package com.ecommerceproject.inventorymanagementservice.repositories;

import com.ecommerceproject.inventorymanagementservice.models.InventoryOperation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface InventoryOperationRepository extends JpaRepository<InventoryOperation, Long> {

    Optional<InventoryOperation> findByOperationId(String operationId);
}