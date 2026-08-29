package com.ecommerceproject.inventorymanagementservice.services;

import com.ecommerceproject.inventorymanagementservice.dtos.requestdto.ReleaseInventoryRequestDto;
import com.ecommerceproject.inventorymanagementservice.dtos.requestdto.ReserveInventoryRequestDto;
import com.ecommerceproject.inventorymanagementservice.dtos.responsedto.InventoryResponseDto;

public interface IInventoryService {

    InventoryResponseDto getProductInventory(Long productId);

    InventoryResponseDto reserveProduct(ReserveInventoryRequestDto requestDto);

    InventoryResponseDto releaseProduct(ReleaseInventoryRequestDto requestDto);
}
