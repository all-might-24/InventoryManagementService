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
import com.ecommerceproject.inventorymanagementservice.models.InventoryOperation;
import com.ecommerceproject.inventorymanagementservice.models.enums.InventoryOperationType;
import com.ecommerceproject.inventorymanagementservice.repositories.InventoryOperationRepository;
import com.ecommerceproject.inventorymanagementservice.repositories.InventoryRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class InventoryService implements IInventoryService{

    private final InventoryRepository inventoryRepository;
    private final InventoryMapper inventoryMapper;
    private final InventoryOperationRepository inventoryOperationRepository;

    public InventoryService(InventoryRepository inventoryRepository,
                            InventoryMapper inventoryMapper,
                            InventoryOperationRepository inventoryOperationRepository) {
        this.inventoryRepository = inventoryRepository;
        this.inventoryMapper = inventoryMapper;
        this.inventoryOperationRepository = inventoryOperationRepository;
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

        Optional<InventoryOperation> existing = inventoryOperationRepository.findByOperationId(requestDto.getOperationId());

        if (existing.isPresent()) {
            validateExistingOperation(existing.get(),
                    requestDto.getProductId(),
                    requestDto.getQuantity(),
                    InventoryOperationType.RESERVE);
            Inventory inventory = findInventoryForUpdate(requestDto.getProductId());
            return inventoryMapper.ToResponseDto(inventory);
        }

        Inventory inventory = findInventoryForUpdate(requestDto.getProductId());
        if (inventory.getAvailableQuantity() < requestDto.getQuantity()) {
            throw new InsufficientStockException(requestDto.getProductId(), requestDto.getQuantity(), inventory.getAvailableQuantity());
        }

        // available = available - requestQty
        inventory.setAvailableQuantity(inventory.getAvailableQuantity() - requestDto.getQuantity());
        // reserved = reserved + requestQty
        inventory.setReservedQuantity(inventory.getReservedQuantity() + requestDto.getQuantity());
        Inventory savedInventory = inventoryRepository.save(inventory);

        InventoryOperation operation = new InventoryOperation();

        operation.setOperationId(requestDto.getOperationId());
        operation.setProductId(requestDto.getProductId());
        operation.setQuantity(requestDto.getQuantity());
        operation.setOperationType(InventoryOperationType.RESERVE);

        inventoryOperationRepository.save(operation);

        return inventoryMapper.ToResponseDto(savedInventory);
    }

    @Override
    @Transactional
    public InventoryResponseDto releaseProduct(ReleaseInventoryRequestDto requestDto) {

        Optional<InventoryOperation> existing = inventoryOperationRepository.findByOperationId(requestDto.getOperationId());

        if (existing.isPresent()) {
            validateExistingOperation(existing.get(),
                    requestDto.getProductId(),
                    requestDto.getQuantity(),
                    InventoryOperationType.RELEASE);
            Inventory inventory = findInventoryForUpdate(requestDto.getProductId());
            return inventoryMapper.ToResponseDto(inventory);
        }

        Inventory inventory = findInventoryForUpdate(requestDto.getProductId());

        if (requestDto.getQuantity() > inventory.getReservedQuantity()) {
            throw new InvalidInventoryOperationException("release",requestDto.getQuantity(), requestDto.getProductId(), inventory.getReservedQuantity());
        }

        // reserved = reserved - requestQty
        inventory.setReservedQuantity(inventory.getReservedQuantity() - requestDto.getQuantity());

        // available = available + requestQty
        inventory.setAvailableQuantity(inventory.getAvailableQuantity() + requestDto.getQuantity());

        Inventory savedInventory = inventoryRepository.save(inventory);


        InventoryOperation operation = new InventoryOperation();

        operation.setOperationId(requestDto.getOperationId());
        operation.setProductId(requestDto.getProductId());
        operation.setQuantity(requestDto.getQuantity());
        operation.setOperationType(InventoryOperationType.RELEASE);

        inventoryOperationRepository.save(operation);

        return inventoryMapper.ToResponseDto(savedInventory);
    }

    @Override
    @Transactional
    public InventoryResponseDto commitProduct(CommitInventoryRequestDto requestDto) {
        Optional<InventoryOperation> existing = inventoryOperationRepository.findByOperationId(requestDto.getOperationId());

        if (existing.isPresent()) {
            validateExistingOperation(existing.get(),
                    requestDto.getProductId(),
                    requestDto.getQuantity(),
                    InventoryOperationType.COMMIT);
            Inventory inventory = findInventoryForUpdate(requestDto.getProductId());
            return inventoryMapper.ToResponseDto(inventory);
        }

        Inventory inventory = findInventoryForUpdate(requestDto.getProductId());

        if (requestDto.getQuantity() > inventory.getReservedQuantity()) {
            throw new InvalidInventoryOperationException("commit", requestDto.getQuantity(), requestDto.getProductId(), inventory.getReservedQuantity());
        }

        // reserved = reserved - requestQty
        inventory.setReservedQuantity(inventory.getReservedQuantity() - requestDto.getQuantity());

        // sold = sold + requestQty
        inventory.setSoldQuantity(inventory.getSoldQuantity() + requestDto.getQuantity());

        Inventory savedInventory = inventoryRepository.save(inventory);


        InventoryOperation operation = new InventoryOperation();

        operation.setOperationId(requestDto.getOperationId());
        operation.setProductId(requestDto.getProductId());
        operation.setQuantity(requestDto.getQuantity());
        operation.setOperationType(InventoryOperationType.COMMIT);

        inventoryOperationRepository.save(operation);

        return inventoryMapper.ToResponseDto(savedInventory);
    }

    private Inventory findInventoryForUpdate(Long productId) {
        Optional<Inventory> optional = inventoryRepository.findByProductIdForUpdate(productId);
        if(optional.isEmpty()) {
            throw new InventoryNotFoundException("Inventory not found with Product id #" + productId);
        }
        return optional.get();
    }

    private void validateExistingOperation(InventoryOperation existing, Long productId, Integer quantity, InventoryOperationType expectedType) {
        if (!existing.getProductId().equals(productId)
                || !existing.getQuantity().equals(quantity)
                || existing.getOperationType() != expectedType) {

            throw new InvalidInventoryOperationException("Idempotency key already used for a different inventory operation");
        }
    }

}
