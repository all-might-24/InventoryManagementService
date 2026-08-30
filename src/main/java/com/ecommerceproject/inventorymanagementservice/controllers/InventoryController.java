package com.ecommerceproject.inventorymanagementservice.controllers;

import com.ecommerceproject.inventorymanagementservice.dtos.requestdto.CommitInventoryRequestDto;
import com.ecommerceproject.inventorymanagementservice.dtos.requestdto.ReleaseInventoryRequestDto;
import com.ecommerceproject.inventorymanagementservice.dtos.requestdto.ReserveInventoryRequestDto;
import com.ecommerceproject.inventorymanagementservice.dtos.responsedto.InventoryResponseDto;
import com.ecommerceproject.inventorymanagementservice.services.IInventoryService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/inventory")
public class InventoryController {

    private final IInventoryService inventoryService;

    public InventoryController(IInventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    @GetMapping("/products/{productId}")
    public ResponseEntity<InventoryResponseDto> getInventoryByProductId(@PathVariable("productId") Long productId) {
        InventoryResponseDto responseDto = inventoryService.getProductInventory(productId);

        return ResponseEntity.ok(responseDto);
    }

    @PostMapping("/reserve")
    public ResponseEntity<InventoryResponseDto> postReserveProduct(@Valid @RequestBody ReserveInventoryRequestDto requestDto) {
        InventoryResponseDto responseDto = inventoryService.reserveProduct(requestDto);

        return ResponseEntity.ok(responseDto);
    }

    @PostMapping("/release")
    public ResponseEntity<InventoryResponseDto> postReleaseProduct(@Valid @RequestBody ReleaseInventoryRequestDto requestDto) {
        InventoryResponseDto responseDto = inventoryService.releaseProduct(requestDto);

        return ResponseEntity.ok(responseDto);
    }

    @PostMapping("/commit")
    public ResponseEntity<InventoryResponseDto> postCommitProduct(@Valid @RequestBody CommitInventoryRequestDto requestDto) {
        InventoryResponseDto responseDto = inventoryService.commitProduct(requestDto);

        return ResponseEntity.ok(responseDto);
    }
}
