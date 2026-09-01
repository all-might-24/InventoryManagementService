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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InventoryServiceTest {

    @Mock
    private InventoryRepository inventoryRepository;

    @Mock
    private InventoryMapper inventoryMapper;

    @Mock
    private InventoryOperationRepository inventoryOperationRepository;

    @InjectMocks
    private InventoryService inventoryService;


/*
 * =========================================================
 * GET INVENTORY
 * =========================================================
 */

    @Test
    void getProductInventory_whenInventoryExists_shouldReturnInventory() {

        Inventory inventory =
                createInventory(
                        10L,
                        7,
                        3,
                        2
                );

        InventoryResponseDto expectedResponse =
                createResponse(
                        10L,
                        7,
                        3,
                        2
                );

        when(inventoryRepository.findByProductId(10L))
                .thenReturn(Optional.of(inventory));

        when(inventoryMapper.ToResponseDto(inventory))
                .thenReturn(expectedResponse);

        InventoryResponseDto result =
                inventoryService.getProductInventory(10L);

        assertSame(expectedResponse, result);

        verify(inventoryRepository)
                .findByProductId(10L);

        verify(inventoryMapper)
                .ToResponseDto(inventory);
    }

    @Test
    void getProductInventory_whenInventoryDoesNotExist_shouldThrowInventoryNotFoundException() {

        when(inventoryRepository.findByProductId(10L))
                .thenReturn(Optional.empty());

        InventoryNotFoundException exception =
                assertThrows(
                        InventoryNotFoundException.class,
                        () -> inventoryService.getProductInventory(10L)
                );

        assertEquals(
                "Inventory not found with Product id #10",
                exception.getMessage()
        );

        verify(inventoryMapper, never())
                .ToResponseDto(any());
    }


/*
 * =========================================================
 * RESERVE
 * =========================================================
 */

    @Test
    void reserveProduct_whenStockIsAvailable_shouldMoveAvailableToReserved() {

        ReserveInventoryRequestDto request =
                createReserveRequest(
                        10L,
                        3,
                        "reserve-123"
                );

        Inventory inventory =
                createInventory(
                        10L,
                        10,
                        2,
                        1
                );

        InventoryResponseDto expectedResponse =
                createResponse(
                        10L,
                        7,
                        5,
                        1
                );

        when(inventoryOperationRepository.findByOperationId("reserve-123"))
                .thenReturn(Optional.empty());

        when(inventoryRepository.findByProductIdForUpdate(10L))
                .thenReturn(Optional.of(inventory));

        when(inventoryRepository.save(inventory))
                .thenReturn(inventory);

        when(inventoryMapper.ToResponseDto(inventory))
                .thenReturn(expectedResponse);

        InventoryResponseDto result =
                inventoryService.reserveProduct(request);

        assertEquals(7, inventory.getAvailableQuantity());
        assertEquals(5, inventory.getReservedQuantity());
        assertEquals(1, inventory.getSoldQuantity());

        assertSame(expectedResponse, result);

        verify(inventoryRepository)
                .save(inventory);

        verify(inventoryOperationRepository)
                .save(any(InventoryOperation.class));
    }

    @Test
    void reserveProduct_shouldSaveReserveOperation() {

        ReserveInventoryRequestDto request =
                createReserveRequest(
                        10L,
                        3,
                        "reserve-123"
                );

        Inventory inventory =
                createInventory(
                        10L,
                        10,
                        0,
                        0
                );

        when(inventoryOperationRepository.findByOperationId("reserve-123"))
                .thenReturn(Optional.empty());

        when(inventoryRepository.findByProductIdForUpdate(10L))
                .thenReturn(Optional.of(inventory));

        when(inventoryRepository.save(inventory))
                .thenReturn(inventory);

        when(inventoryMapper.ToResponseDto(inventory))
                .thenReturn(new InventoryResponseDto());

        inventoryService.reserveProduct(request);

        ArgumentCaptor<InventoryOperation> captor =
                ArgumentCaptor.forClass(InventoryOperation.class);

        verify(inventoryOperationRepository)
                .save(captor.capture());

        InventoryOperation operation =
                captor.getValue();

        assertEquals(
                "reserve-123",
                operation.getOperationId()
        );

        assertEquals(
                10L,
                operation.getProductId()
        );

        assertEquals(
                3,
                operation.getQuantity()
        );

        assertEquals(
                InventoryOperationType.RESERVE,
                operation.getOperationType()
        );
    }

    @Test
    void reserveProduct_whenStockIsInsufficient_shouldThrowInsufficientStockException() {

        ReserveInventoryRequestDto request =
                createReserveRequest(
                        10L,
                        8,
                        "reserve-123"
                );

        Inventory inventory =
                createInventory(
                        10L,
                        7,
                        0,
                        0
                );

        when(inventoryOperationRepository.findByOperationId("reserve-123"))
                .thenReturn(Optional.empty());

        when(inventoryRepository.findByProductIdForUpdate(10L))
                .thenReturn(Optional.of(inventory));

        InsufficientStockException exception =
                assertThrows(
                        InsufficientStockException.class,
                        () -> inventoryService.reserveProduct(request)
                );

        assertEquals(
                "Insufficient stock for product 10. Requested: 8, Available: 7",
                exception.getMessage()
        );

        assertEquals(7, inventory.getAvailableQuantity());
        assertEquals(0, inventory.getReservedQuantity());

        verify(inventoryRepository, never())
                .save(any());

        verify(inventoryOperationRepository, never())
                .save(any());
    }

    @Test
    void reserveProduct_whenInventoryDoesNotExist_shouldThrowInventoryNotFoundException() {

        ReserveInventoryRequestDto request =
                createReserveRequest(
                        10L,
                        3,
                        "reserve-123"
                );

        when(inventoryOperationRepository.findByOperationId("reserve-123"))
                .thenReturn(Optional.empty());

        when(inventoryRepository.findByProductIdForUpdate(10L))
                .thenReturn(Optional.empty());

        InventoryNotFoundException exception =
                assertThrows(
                        InventoryNotFoundException.class,
                        () -> inventoryService.reserveProduct(request)
                );

        assertEquals(
                "Inventory not found with Product id #10",
                exception.getMessage()
        );

        verify(inventoryRepository, never())
                .save(any());
    }


    /*
     * =========================================================
     * RESERVE IDEMPOTENCY
     * =========================================================
     */

    @Test
    void reserveProduct_whenSameOperationAlreadyExists_shouldNotReserveAgain() {

        ReserveInventoryRequestDto request =
                createReserveRequest(
                        10L,
                        3,
                        "reserve-123"
                );

        InventoryOperation existingOperation =
                createOperation(
                        "reserve-123",
                        10L,
                        3,
                        InventoryOperationType.RESERVE
                );

        Inventory inventory =
                createInventory(
                        10L,
                        7,
                        3,
                        0
                );

        InventoryResponseDto expectedResponse =
                createResponse(
                        10L,
                        7,
                        3,
                        0
                );

        when(inventoryOperationRepository.findByOperationId("reserve-123"))
                .thenReturn(Optional.of(existingOperation));

        when(inventoryRepository.findByProductIdForUpdate(10L))
                .thenReturn(Optional.of(inventory));

        when(inventoryMapper.ToResponseDto(inventory))
                .thenReturn(expectedResponse);

        InventoryResponseDto result =
                inventoryService.reserveProduct(request);

        assertSame(expectedResponse, result);

        assertEquals(7, inventory.getAvailableQuantity());
        assertEquals(3, inventory.getReservedQuantity());

        verify(inventoryRepository, never())
                .save(any());

        verify(inventoryOperationRepository, never())
                .save(any());
    }

    @Test
    void reserveProduct_whenOperationIdWasUsedForDifferentProduct_shouldThrowInvalidInventoryOperationException() {

        ReserveInventoryRequestDto request =
                createReserveRequest(
                        20L,
                        3,
                        "reserve-123"
                );

        InventoryOperation existing =
                createOperation(
                        "reserve-123",
                        10L,
                        3,
                        InventoryOperationType.RESERVE
                );

        when(inventoryOperationRepository.findByOperationId("reserve-123"))
                .thenReturn(Optional.of(existing));

        InvalidInventoryOperationException exception =
                assertThrows(
                        InvalidInventoryOperationException.class,
                        () -> inventoryService.reserveProduct(request)
                );

        assertEquals(
                "Idempotency key already used for a different inventory operation",
                exception.getMessage()
        );

        verify(inventoryRepository, never())
                .findByProductIdForUpdate(any());
    }

    @Test
    void reserveProduct_whenOperationIdWasUsedForDifferentQuantity_shouldThrowInvalidInventoryOperationException() {

        ReserveInventoryRequestDto request =
                createReserveRequest(
                        10L,
                        5,
                        "reserve-123"
                );

        InventoryOperation existing =
                createOperation(
                        "reserve-123",
                        10L,
                        3,
                        InventoryOperationType.RESERVE
                );

        when(inventoryOperationRepository.findByOperationId("reserve-123"))
                .thenReturn(Optional.of(existing));

        assertThrows(
                InvalidInventoryOperationException.class,
                () -> inventoryService.reserveProduct(request)
        );

        verify(inventoryRepository, never())
                .findByProductIdForUpdate(any());
    }

    @Test
    void reserveProduct_whenOperationIdWasUsedForDifferentOperationType_shouldThrowInvalidInventoryOperationException() {

        ReserveInventoryRequestDto request =
                createReserveRequest(
                        10L,
                        3,
                        "operation-123"
                );

        InventoryOperation existing =
                createOperation(
                        "operation-123",
                        10L,
                        3,
                        InventoryOperationType.RELEASE
                );

        when(inventoryOperationRepository.findByOperationId("operation-123"))
                .thenReturn(Optional.of(existing));

        assertThrows(
                InvalidInventoryOperationException.class,
                () -> inventoryService.reserveProduct(request)
        );

        verify(inventoryRepository, never())
                .findByProductIdForUpdate(any());
    }


    /*
     * =========================================================
     * RELEASE
     * =========================================================
     */

    @Test
    void releaseProduct_whenReservedStockExists_shouldMoveReservedToAvailable() {

        ReleaseInventoryRequestDto request =
                createReleaseRequest(
                        10L,
                        3,
                        "release-123"
                );

        Inventory inventory =
                createInventory(
                        10L,
                        7,
                        5,
                        1
                );

        InventoryResponseDto expectedResponse =
                createResponse(
                        10L,
                        10,
                        2,
                        1
                );

        when(inventoryOperationRepository.findByOperationId("release-123"))
                .thenReturn(Optional.empty());

        when(inventoryRepository.findByProductIdForUpdate(10L))
                .thenReturn(Optional.of(inventory));

        when(inventoryRepository.save(inventory))
                .thenReturn(inventory);

        when(inventoryMapper.ToResponseDto(inventory))
                .thenReturn(expectedResponse);

        InventoryResponseDto result =
                inventoryService.releaseProduct(request);

        assertEquals(10, inventory.getAvailableQuantity());
        assertEquals(2, inventory.getReservedQuantity());
        assertEquals(1, inventory.getSoldQuantity());

        assertSame(expectedResponse, result);

        verify(inventoryRepository)
                .save(inventory);
    }

    @Test
    void releaseProduct_shouldSaveReleaseOperation() {

        ReleaseInventoryRequestDto request =
                createReleaseRequest(
                        10L,
                        3,
                        "release-123"
                );

        Inventory inventory =
                createInventory(
                        10L,
                        7,
                        5,
                        0
                );

        when(inventoryOperationRepository.findByOperationId("release-123"))
                .thenReturn(Optional.empty());

        when(inventoryRepository.findByProductIdForUpdate(10L))
                .thenReturn(Optional.of(inventory));

        when(inventoryRepository.save(inventory))
                .thenReturn(inventory);

        when(inventoryMapper.ToResponseDto(inventory))
                .thenReturn(new InventoryResponseDto());

        inventoryService.releaseProduct(request);

        ArgumentCaptor<InventoryOperation> captor =
                ArgumentCaptor.forClass(InventoryOperation.class);

        verify(inventoryOperationRepository)
                .save(captor.capture());

        InventoryOperation operation =
                captor.getValue();

        assertEquals(
                "release-123",
                operation.getOperationId()
        );

        assertEquals(
                10L,
                operation.getProductId()
        );

        assertEquals(
                3,
                operation.getQuantity()
        );

        assertEquals(
                InventoryOperationType.RELEASE,
                operation.getOperationType()
        );
    }

    @Test
    void releaseProduct_whenQuantityExceedsReserved_shouldThrowInvalidInventoryOperationException() {

        ReleaseInventoryRequestDto request =
                createReleaseRequest(
                        10L,
                        4,
                        "release-123"
                );

        Inventory inventory =
                createInventory(
                        10L,
                        7,
                        3,
                        0
                );

        when(inventoryOperationRepository.findByOperationId("release-123"))
                .thenReturn(Optional.empty());

        when(inventoryRepository.findByProductIdForUpdate(10L))
                .thenReturn(Optional.of(inventory));

        InvalidInventoryOperationException exception =
                assertThrows(
                        InvalidInventoryOperationException.class,
                        () -> inventoryService.releaseProduct(request)
                );

        assertEquals(
                "Cannot release 4 units for product 10. Only 3 units are reserved",
                exception.getMessage()
        );

        assertEquals(7, inventory.getAvailableQuantity());
        assertEquals(3, inventory.getReservedQuantity());

        verify(inventoryRepository, never())
                .save(any());

        verify(inventoryOperationRepository, never())
                .save(any());
    }

    @Test
    void releaseProduct_whenSameOperationAlreadyExists_shouldNotReleaseAgain() {

        ReleaseInventoryRequestDto request =
                createReleaseRequest(
                        10L,
                        3,
                        "release-123"
                );

        InventoryOperation existing =
                createOperation(
                        "release-123",
                        10L,
                        3,
                        InventoryOperationType.RELEASE
                );

        Inventory inventory =
                createInventory(
                        10L,
                        10,
                        2,
                        0
                );

        when(inventoryOperationRepository.findByOperationId("release-123"))
                .thenReturn(Optional.of(existing));

        when(inventoryRepository.findByProductIdForUpdate(10L))
                .thenReturn(Optional.of(inventory));

        when(inventoryMapper.ToResponseDto(inventory))
                .thenReturn(new InventoryResponseDto());

        inventoryService.releaseProduct(request);

        assertEquals(10, inventory.getAvailableQuantity());
        assertEquals(2, inventory.getReservedQuantity());

        verify(inventoryRepository, never())
                .save(any());

        verify(inventoryOperationRepository, never())
                .save(any());
    }


    /*
     * =========================================================
     * COMMIT
     * =========================================================
     */

    @Test
    void commitProduct_whenReservedStockExists_shouldMoveReservedToSold() {

        CommitInventoryRequestDto request =
                createCommitRequest(
                        10L,
                        3,
                        "commit-123"
                );

        Inventory inventory =
                createInventory(
                        10L,
                        7,
                        5,
                        2
                );

        InventoryResponseDto expectedResponse =
                createResponse(
                        10L,
                        7,
                        2,
                        5
                );

        when(inventoryOperationRepository.findByOperationId("commit-123"))
                .thenReturn(Optional.empty());

        when(inventoryRepository.findByProductIdForUpdate(10L))
                .thenReturn(Optional.of(inventory));

        when(inventoryRepository.save(inventory))
                .thenReturn(inventory);

        when(inventoryMapper.ToResponseDto(inventory))
                .thenReturn(expectedResponse);

        InventoryResponseDto result =
                inventoryService.commitProduct(request);

        assertEquals(7, inventory.getAvailableQuantity());
        assertEquals(2, inventory.getReservedQuantity());
        assertEquals(5, inventory.getSoldQuantity());

        assertSame(expectedResponse, result);

        verify(inventoryRepository)
                .save(inventory);
    }

    @Test
    void commitProduct_shouldSaveCommitOperation() {

        CommitInventoryRequestDto request =
                createCommitRequest(
                        10L,
                        3,
                        "commit-123"
                );

        Inventory inventory =
                createInventory(
                        10L,
                        7,
                        5,
                        0
                );

        when(inventoryOperationRepository.findByOperationId("commit-123"))
                .thenReturn(Optional.empty());

        when(inventoryRepository.findByProductIdForUpdate(10L))
                .thenReturn(Optional.of(inventory));

        when(inventoryRepository.save(inventory))
                .thenReturn(inventory);

        when(inventoryMapper.ToResponseDto(inventory))
                .thenReturn(new InventoryResponseDto());

        inventoryService.commitProduct(request);

        ArgumentCaptor<InventoryOperation> captor =
                ArgumentCaptor.forClass(InventoryOperation.class);

        verify(inventoryOperationRepository)
                .save(captor.capture());

        InventoryOperation operation =
                captor.getValue();

        assertEquals(
                "commit-123",
                operation.getOperationId()
        );

        assertEquals(
                10L,
                operation.getProductId()
        );

        assertEquals(
                3,
                operation.getQuantity()
        );

        assertEquals(
                InventoryOperationType.COMMIT,
                operation.getOperationType()
        );
    }

    @Test
    void commitProduct_whenQuantityExceedsReserved_shouldThrowInvalidInventoryOperationException() {

        CommitInventoryRequestDto request =
                createCommitRequest(
                        10L,
                        5,
                        "commit-123"
                );

        Inventory inventory =
                createInventory(
                        10L,
                        7,
                        3,
                        2
                );

        when(inventoryOperationRepository.findByOperationId("commit-123"))
                .thenReturn(Optional.empty());

        when(inventoryRepository.findByProductIdForUpdate(10L))
                .thenReturn(Optional.of(inventory));

        InvalidInventoryOperationException exception =
                assertThrows(
                        InvalidInventoryOperationException.class,
                        () -> inventoryService.commitProduct(request)
                );

        assertEquals(
                "Cannot commit 5 units for product 10. Only 3 units are reserved",
                exception.getMessage()
        );

        assertEquals(3, inventory.getReservedQuantity());
        assertEquals(2, inventory.getSoldQuantity());

        verify(inventoryRepository, never())
                .save(any());

        verify(inventoryOperationRepository, never())
                .save(any());
    }

    @Test
    void commitProduct_whenSameOperationAlreadyExists_shouldNotCommitAgain() {

        CommitInventoryRequestDto request =
                createCommitRequest(
                        10L,
                        3,
                        "commit-123"
                );

        InventoryOperation existing =
                createOperation(
                        "commit-123",
                        10L,
                        3,
                        InventoryOperationType.COMMIT
                );

        Inventory inventory =
                createInventory(
                        10L,
                        7,
                        2,
                        3
                );

        when(inventoryOperationRepository.findByOperationId("commit-123"))
                .thenReturn(Optional.of(existing));

        when(inventoryRepository.findByProductIdForUpdate(10L))
                .thenReturn(Optional.of(inventory));

        when(inventoryMapper.ToResponseDto(inventory))
                .thenReturn(new InventoryResponseDto());

        inventoryService.commitProduct(request);

        assertEquals(2, inventory.getReservedQuantity());
        assertEquals(3, inventory.getSoldQuantity());

        verify(inventoryRepository, never())
                .save(any());

        verify(inventoryOperationRepository, never())
                .save(any());
    }


    /*
     * =========================================================
     * SHARED IDEMPOTENCY CONFLICT
     * =========================================================
     */

    @Test
    void releaseProduct_whenOperationIdBelongsToReserve_shouldThrowInvalidInventoryOperationException() {

        ReleaseInventoryRequestDto request =
                createReleaseRequest(
                        10L,
                        3,
                        "operation-123"
                );

        InventoryOperation existing =
                createOperation(
                        "operation-123",
                        10L,
                        3,
                        InventoryOperationType.RESERVE
                );

        when(inventoryOperationRepository.findByOperationId("operation-123"))
                .thenReturn(Optional.of(existing));

        assertThrows(
                InvalidInventoryOperationException.class,
                () -> inventoryService.releaseProduct(request)
        );

        verify(inventoryRepository, never())
                .findByProductIdForUpdate(any());
    }

    @Test
    void commitProduct_whenOperationIdBelongsToRelease_shouldThrowInvalidInventoryOperationException() {

        CommitInventoryRequestDto request =
                createCommitRequest(
                        10L,
                        3,
                        "operation-123"
                );

        InventoryOperation existing =
                createOperation(
                        "operation-123",
                        10L,
                        3,
                        InventoryOperationType.RELEASE
                );

        when(inventoryOperationRepository.findByOperationId("operation-123"))
                .thenReturn(Optional.of(existing));

        assertThrows(
                InvalidInventoryOperationException.class,
                () -> inventoryService.commitProduct(request)
        );

        verify(inventoryRepository, never())
                .findByProductIdForUpdate(any());
    }


    /*
     * =========================================================
     * HELPERS
     * =========================================================
     */

    private Inventory createInventory(
            Long productId,
            Integer available,
            Integer reserved,
            Integer sold) {

        Inventory inventory =
                new Inventory();

        inventory.setProductId(productId);
        inventory.setAvailableQuantity(available);
        inventory.setReservedQuantity(reserved);
        inventory.setSoldQuantity(sold);

        return inventory;
    }

    private InventoryResponseDto createResponse(
            Long productId,
            Integer available,
            Integer reserved,
            Integer sold) {

        InventoryResponseDto response =
                new InventoryResponseDto();

        response.setProductId(productId);
        response.setAvailableQuantity(available);
        response.setReservedQuantity(reserved);
        response.setSoldQuantity(sold);

        return response;
    }

    private InventoryOperation createOperation(
            String operationId,
            Long productId,
            Integer quantity,
            InventoryOperationType operationType) {

        InventoryOperation operation =
                new InventoryOperation();

        operation.setOperationId(operationId);
        operation.setProductId(productId);
        operation.setQuantity(quantity);
        operation.setOperationType(operationType);

        return operation;
    }

    private ReserveInventoryRequestDto createReserveRequest(
            Long productId,
            Integer quantity,
            String operationId) {

        ReserveInventoryRequestDto request =
                new ReserveInventoryRequestDto();

        request.setProductId(productId);
        request.setQuantity(quantity);
        request.setOperationId(operationId);

        return request;
    }

    private ReleaseInventoryRequestDto createReleaseRequest(
            Long productId,
            Integer quantity,
            String operationId) {

        ReleaseInventoryRequestDto request =
                new ReleaseInventoryRequestDto();

        request.setProductId(productId);
        request.setQuantity(quantity);
        request.setOperationId(operationId);

        return request;
    }

    private CommitInventoryRequestDto createCommitRequest(
            Long productId,
            Integer quantity,
            String operationId) {

        CommitInventoryRequestDto request =
                new CommitInventoryRequestDto();

        request.setProductId(productId);
        request.setQuantity(quantity);
        request.setOperationId(operationId);

        return request;
    }
}