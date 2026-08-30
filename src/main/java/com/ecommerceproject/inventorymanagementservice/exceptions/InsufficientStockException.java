package com.ecommerceproject.inventorymanagementservice.exceptions;

public class InsufficientStockException extends RuntimeException{

    public InsufficientStockException(Long productId, Integer requestQty, Integer availableQty) {
        super("Insufficient stock for product " + productId
                + ". Requested: " + requestQty
                + ", Available: " + availableQty);
    }
}
