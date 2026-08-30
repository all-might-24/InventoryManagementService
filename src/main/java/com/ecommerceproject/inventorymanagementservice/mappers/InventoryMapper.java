package com.ecommerceproject.inventorymanagementservice.mappers;

import com.ecommerceproject.inventorymanagementservice.dtos.responsedto.InventoryResponseDto;
import com.ecommerceproject.inventorymanagementservice.models.Inventory;
import org.springframework.stereotype.Component;

@Component
public class InventoryMapper {

    public InventoryResponseDto ToResponseDto(Inventory inventory) {

        InventoryResponseDto response = new InventoryResponseDto();

        response.setProductId(inventory.getProductId());
        response.setAvailableQuantity(inventory.getAvailableQuantity());
        response.setReservedQuantity(inventory.getReservedQuantity());
        response.setSoldQuantity(inventory.getSoldQuantity());
        return response;
    }
}
