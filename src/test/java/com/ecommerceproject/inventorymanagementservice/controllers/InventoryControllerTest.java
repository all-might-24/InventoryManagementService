package com.ecommerceproject.inventorymanagementservice.controllers;

import com.ecommerceproject.inventorymanagementservice.dtos.requestdto.CommitInventoryRequestDto;
import com.ecommerceproject.inventorymanagementservice.dtos.requestdto.ReleaseInventoryRequestDto;
import com.ecommerceproject.inventorymanagementservice.dtos.requestdto.ReserveInventoryRequestDto;
import com.ecommerceproject.inventorymanagementservice.dtos.responsedto.InventoryResponseDto;
import com.ecommerceproject.inventorymanagementservice.services.IInventoryService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(InventoryController.class)
class InventoryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private IInventoryService inventoryService;


/*
 * =========================================================
 * GET INVENTORY
 * =========================================================
 */

    @Test
    void getInventoryByProductId_whenInventoryExists_shouldReturn200()
            throws Exception {

        InventoryResponseDto response =
                createInventoryResponse(
                        10L,
                        7,
                        3,
                        2
                );

        when(inventoryService.getProductInventory(10L))
                .thenReturn(response);

        mockMvc.perform(
                        get("/inventory/products/{productId}", 10L)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.productId").value(10))
                .andExpect(jsonPath("$.availableQuantity").value(7))
                .andExpect(jsonPath("$.reservedQuantity").value(3))
                .andExpect(jsonPath("$.soldQuantity").value(2));

        verify(inventoryService)
                .getProductInventory(10L);
    }


/*
 * =========================================================
 * RESERVE
 * =========================================================
 */

    @Test
    void reserveProduct_withValidRequest_shouldReturn200()
            throws Exception {

        ReserveInventoryRequestDto request =
                new ReserveInventoryRequestDto();

        request.setProductId(10L);
        request.setQuantity(3);
        request.setOperationId("reserve-123");

        InventoryResponseDto response =
                createInventoryResponse(
                        10L,
                        7,
                        3,
                        0
                );

        when(inventoryService.reserveProduct(
                any(ReserveInventoryRequestDto.class)
        )).thenReturn(response);

        mockMvc.perform(
                        post("/inventory/reserve")
                                .contentType("application/json")
                                .content(
                                        objectMapper.writeValueAsString(request)
                                )
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.productId").value(10))
                .andExpect(jsonPath("$.availableQuantity").value(7))
                .andExpect(jsonPath("$.reservedQuantity").value(3))
                .andExpect(jsonPath("$.soldQuantity").value(0));

        verify(inventoryService)
                .reserveProduct(
                        any(ReserveInventoryRequestDto.class)
                );
    }

    @Test
    void reserveProduct_withNullProductId_shouldReturn400()
            throws Exception {

        ReserveInventoryRequestDto request =
                createReserveRequest(
                        null,
                        3,
                        "reserve-123"
                );

        performReserveAndExpectBadRequest(request);

        verify(inventoryService, never())
                .reserveProduct(any());
    }

    @Test
    void reserveProduct_withZeroProductId_shouldReturn400()
            throws Exception {

        ReserveInventoryRequestDto request =
                createReserveRequest(
                        0L,
                        3,
                        "reserve-123"
                );

        performReserveAndExpectBadRequest(request);

        verify(inventoryService, never())
                .reserveProduct(any());
    }

    @Test
    void reserveProduct_withNegativeProductId_shouldReturn400()
            throws Exception {

        ReserveInventoryRequestDto request =
                createReserveRequest(
                        -10L,
                        3,
                        "reserve-123"
                );

        performReserveAndExpectBadRequest(request);

        verify(inventoryService, never())
                .reserveProduct(any());
    }

    @Test
    void reserveProduct_withNullQuantity_shouldReturn400()
            throws Exception {

        ReserveInventoryRequestDto request =
                createReserveRequest(
                        10L,
                        null,
                        "reserve-123"
                );

        performReserveAndExpectBadRequest(request);

        verify(inventoryService, never())
                .reserveProduct(any());
    }

    @Test
    void reserveProduct_withZeroQuantity_shouldReturn400()
            throws Exception {

        ReserveInventoryRequestDto request =
                createReserveRequest(
                        10L,
                        0,
                        "reserve-123"
                );

        performReserveAndExpectBadRequest(request);

        verify(inventoryService, never())
                .reserveProduct(any());
    }

    @Test
    void reserveProduct_withNegativeQuantity_shouldReturn400()
            throws Exception {

        ReserveInventoryRequestDto request =
                createReserveRequest(
                        10L,
                        -3,
                        "reserve-123"
                );

        performReserveAndExpectBadRequest(request);

        verify(inventoryService, never())
                .reserveProduct(any());
    }

    @Test
    void reserveProduct_withNullOperationId_shouldReturn400()
            throws Exception {

        ReserveInventoryRequestDto request =
                createReserveRequest(
                        10L,
                        3,
                        null
                );

        performReserveAndExpectBadRequest(request);

        verify(inventoryService, never())
                .reserveProduct(any());
    }

    @Test
    void reserveProduct_withBlankOperationId_shouldReturn400()
            throws Exception {

        ReserveInventoryRequestDto request =
                createReserveRequest(
                        10L,
                        3,
                        "   "
                );

        performReserveAndExpectBadRequest(request);

        verify(inventoryService, never())
                .reserveProduct(any());
    }


/*
 * =========================================================
 * RELEASE
 * =========================================================
 */

    @Test
    void releaseProduct_withValidRequest_shouldReturn200()
            throws Exception {

        ReleaseInventoryRequestDto request =
                new ReleaseInventoryRequestDto();

        request.setProductId(10L);
        request.setQuantity(3);
        request.setOperationId("release-123");

        InventoryResponseDto response =
                createInventoryResponse(
                        10L,
                        10,
                        0,
                        0
                );

        when(inventoryService.releaseProduct(
                any(ReleaseInventoryRequestDto.class)
        )).thenReturn(response);

        mockMvc.perform(
                        post("/inventory/release")
                                .contentType("application/json")
                                .content(
                                        objectMapper.writeValueAsString(request)
                                )
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.productId").value(10))
                .andExpect(jsonPath("$.availableQuantity").value(10))
                .andExpect(jsonPath("$.reservedQuantity").value(0))
                .andExpect(jsonPath("$.soldQuantity").value(0));

        verify(inventoryService)
                .releaseProduct(
                        any(ReleaseInventoryRequestDto.class)
                );
    }

    @Test
    void releaseProduct_withInvalidProductId_shouldReturn400()
            throws Exception {

        ReleaseInventoryRequestDto request =
                createReleaseRequest(
                        0L,
                        3,
                        "release-123"
                );

        mockMvc.perform(
                        post("/inventory/release")
                                .contentType("application/json")
                                .content(
                                        objectMapper.writeValueAsString(request)
                                )
                )
                .andExpect(status().isBadRequest());

        verify(inventoryService, never())
                .releaseProduct(any());
    }

    @Test
    void releaseProduct_withInvalidQuantity_shouldReturn400()
            throws Exception {

        ReleaseInventoryRequestDto request =
                createReleaseRequest(
                        10L,
                        0,
                        "release-123"
                );

        mockMvc.perform(
                        post("/inventory/release")
                                .contentType("application/json")
                                .content(
                                        objectMapper.writeValueAsString(request)
                                )
                )
                .andExpect(status().isBadRequest());

        verify(inventoryService, never())
                .releaseProduct(any());
    }

    @Test
    void releaseProduct_withBlankOperationId_shouldReturn400()
            throws Exception {

        ReleaseInventoryRequestDto request =
                createReleaseRequest(
                        10L,
                        3,
                        ""
                );

        mockMvc.perform(
                        post("/inventory/release")
                                .contentType("application/json")
                                .content(
                                        objectMapper.writeValueAsString(request)
                                )
                )
                .andExpect(status().isBadRequest());

        verify(inventoryService, never())
                .releaseProduct(any());
    }


/*
 * =========================================================
 * COMMIT
 * =========================================================
 */

    @Test
    void commitProduct_withValidRequest_shouldReturn200()
            throws Exception {

        CommitInventoryRequestDto request =
                new CommitInventoryRequestDto();

        request.setProductId(10L);
        request.setQuantity(3);
        request.setOperationId("commit-123");

        InventoryResponseDto response =
                createInventoryResponse(
                        10L,
                        7,
                        0,
                        3
                );

        when(inventoryService.commitProduct(
                any(CommitInventoryRequestDto.class)
        )).thenReturn(response);

        mockMvc.perform(
                        post("/inventory/commit")
                                .contentType("application/json")
                                .content(
                                        objectMapper.writeValueAsString(request)
                                )
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.productId").value(10))
                .andExpect(jsonPath("$.availableQuantity").value(7))
                .andExpect(jsonPath("$.reservedQuantity").value(0))
                .andExpect(jsonPath("$.soldQuantity").value(3));

        verify(inventoryService)
                .commitProduct(
                        any(CommitInventoryRequestDto.class)
                );
    }

    @Test
    void commitProduct_withInvalidProductId_shouldReturn400()
            throws Exception {

        CommitInventoryRequestDto request =
                createCommitRequest(
                        -1L,
                        3,
                        "commit-123"
                );

        mockMvc.perform(
                        post("/inventory/commit")
                                .contentType("application/json")
                                .content(
                                        objectMapper.writeValueAsString(request)
                                )
                )
                .andExpect(status().isBadRequest());

        verify(inventoryService, never())
                .commitProduct(any());
    }

    @Test
    void commitProduct_withInvalidQuantity_shouldReturn400()
            throws Exception {

        CommitInventoryRequestDto request =
                createCommitRequest(
                        10L,
                        -1,
                        "commit-123"
                );

        mockMvc.perform(
                        post("/inventory/commit")
                                .contentType("application/json")
                                .content(
                                        objectMapper.writeValueAsString(request)
                                )
                )
                .andExpect(status().isBadRequest());

        verify(inventoryService, never())
                .commitProduct(any());
    }

    @Test
    void commitProduct_withBlankOperationId_shouldReturn400()
            throws Exception {

        CommitInventoryRequestDto request =
                createCommitRequest(
                        10L,
                        3,
                        " "
                );

        mockMvc.perform(
                        post("/inventory/commit")
                                .contentType("application/json")
                                .content(
                                        objectMapper.writeValueAsString(request)
                                )
                )
                .andExpect(status().isBadRequest());

        verify(inventoryService, never())
                .commitProduct(any());
    }


/*
 * =========================================================
 * HELPERS
 * =========================================================
 */

    private void performReserveAndExpectBadRequest(ReserveInventoryRequestDto request) throws Exception {

        mockMvc.perform(
                        post("/inventory/reserve")
                                .contentType("application/json")
                                .content(
                                        objectMapper.writeValueAsString(request)
                                )
                )
                .andExpect(status().isBadRequest());
    }

    private ReserveInventoryRequestDto createReserveRequest(
            Long productId,
            Integer quantity,
            String operationId
    ) {

        ReserveInventoryRequestDto request = new ReserveInventoryRequestDto();

        request.setProductId(productId);
        request.setQuantity(quantity);
        request.setOperationId(operationId);

        return request;
    }

    private ReleaseInventoryRequestDto createReleaseRequest(
            Long productId,
            Integer quantity,
            String operationId
    ) {

        ReleaseInventoryRequestDto request = new ReleaseInventoryRequestDto();

        request.setProductId(productId);
        request.setQuantity(quantity);
        request.setOperationId(operationId);

        return request;
    }

    private CommitInventoryRequestDto createCommitRequest(
            Long productId,
            Integer quantity,
            String operationId
    ) {

        CommitInventoryRequestDto request = new CommitInventoryRequestDto();

        request.setProductId(productId);
        request.setQuantity(quantity);
        request.setOperationId(operationId);

        return request;
    }

    private InventoryResponseDto createInventoryResponse(
            Long productId,
            Integer availableQuantity,
            Integer reservedQuantity,
            Integer soldQuantity
    ) {

        InventoryResponseDto response = new InventoryResponseDto();

        response.setProductId(productId);
        response.setAvailableQuantity(availableQuantity);
        response.setReservedQuantity(reservedQuantity);
        response.setSoldQuantity(soldQuantity);

        return response;
    }
}