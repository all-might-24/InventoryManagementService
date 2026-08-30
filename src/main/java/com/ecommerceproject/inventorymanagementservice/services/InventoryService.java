package com.ecommerceproject.inventorymanagementservice.services;

import com.ecommerceproject.inventorymanagementservice.dtos.requestdto.CommitInventoryRequestDto;
import com.ecommerceproject.inventorymanagementservice.dtos.requestdto.ReleaseInventoryRequestDto;
import com.ecommerceproject.inventorymanagementservice.dtos.requestdto.ReserveInventoryRequestDto;
import com.ecommerceproject.inventorymanagementservice.dtos.responsedto.InventoryResponseDto;
import com.ecommerceproject.inventorymanagementservice.exceptions.InsufficientStockException;
import com.ecommerceproject.inventorymanagementservice.exceptions.InvalidInventoryOperationException;
import com.ecommerceproject.inventorymanagementservice.exceptions.InventoryNotFoundException;
import com.ecommerceproject.inventorymanagementservice.mappers.InventoryMapper;
import com.ecommerceproject.inventorymanagementservice.models.Inventory;
import com.ecommerceproject.inventorymanagementservice.repositories.InventoryRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class InventoryService implements IInventoryService{

    private final InventoryRepository inventoryRepository;
    private final InventoryMapper inventoryMapper;

    public InventoryService(InventoryRepository inventoryRepository, InventoryMapper inventoryMapper) {
        this.inventoryRepository = inventoryRepository;
        this.inventoryMapper = inventoryMapper;
    }

    @Override
    @Transactional(readOnly = true)
    public InventoryResponseDto getProductInventory(Long productId) {
        Optional<Inventory> optional = inventoryRepository.findByProductId(productId);
        if(optional.isEmpty()) {
            throw new InventoryNotFoundException("Inventory not found with Product id #" + productId);
        }
        return inventoryMapper.ToResponseDto(optional.get());
    }

    @Override
    @Transactional
    public InventoryResponseDto reserveProduct(ReserveInventoryRequestDto requestDto) {
        Optional<Inventory> optional = inventoryRepository.findByProductIdForUpdate(requestDto.getProductId());
        if(optional.isEmpty()) {
            throw new InventoryNotFoundException("Inventory not found with Product id #" + requestDto.getProductId());
        }
        Inventory inventory = optional.get();
        if (inventory.getAvailableQuantity() < requestDto.getQuantity()) {
            throw new InsufficientStockException(requestDto.getProductId(), requestDto.getQuantity(), inventory.getAvailableQuantity());
        }

        // available = available - requestQty
        inventory.setAvailableQuantity(inventory.getAvailableQuantity() - requestDto.getQuantity());
        // reserved = reserved + requestQty
        inventory.setReservedQuantity(inventory.getReservedQuantity() + requestDto.getQuantity());
        Inventory savedInventory = inventoryRepository.save(inventory);

        return inventoryMapper.ToResponseDto(savedInventory);
    }

    @Override
    @Transactional
    public InventoryResponseDto releaseProduct(ReleaseInventoryRequestDto requestDto) {
        Optional<Inventory> optional = inventoryRepository.findByProductIdForUpdate(requestDto.getProductId());
        if(optional.isEmpty()) {
            throw new InventoryNotFoundException("Inventory not found with Product id #" + requestDto.getProductId());
        }
        Inventory inventory = optional.get();

        if (requestDto.getQuantity() > inventory.getReservedQuantity()) {

            throw new InvalidInventoryOperationException("release",requestDto.getQuantity(), requestDto.getProductId(), inventory.getReservedQuantity());
        }

        // reserved = reserved - requestQty
        inventory.setReservedQuantity(inventory.getReservedQuantity() - requestDto.getQuantity());

        // available = available + requestQty
        inventory.setAvailableQuantity(inventory.getAvailableQuantity() + requestDto.getQuantity());

        Inventory savedInventory = inventoryRepository.save(inventory);

        return inventoryMapper.ToResponseDto(savedInventory);
    }

    @Override
    @Transactional
    public InventoryResponseDto commitProduct(CommitInventoryRequestDto requestDto) {
        Optional<Inventory> optional = inventoryRepository.findByProductIdForUpdate(requestDto.getProductId());
        if(optional.isEmpty()) {
            throw new InventoryNotFoundException("Inventory not found with Product id #" + requestDto.getProductId());
        }
        Inventory inventory = optional.get();

        if (requestDto.getQuantity() > inventory.getReservedQuantity()) {

            throw new InvalidInventoryOperationException("commit", requestDto.getQuantity(), requestDto.getProductId(), inventory.getReservedQuantity());
        }

        // reserved = reserved - requestQty
        inventory.setReservedQuantity(inventory.getReservedQuantity() - requestDto.getQuantity());

        // sold = sold + requestQty
        inventory.setSoldQuantity(inventory.getSoldQuantity() + requestDto.getQuantity());

        Inventory savedInventory = inventoryRepository.save(inventory);

        return inventoryMapper.ToResponseDto(savedInventory);
    }
}
