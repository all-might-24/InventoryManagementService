package com.ecommerceproject.inventorymanagementservice.exceptions;

public class InvalidInventoryOperationException extends RuntimeException{

    public InvalidInventoryOperationException(String message) {
        super(message);
    }

    public InvalidInventoryOperationException(String action, Integer requestQty, Long productId, Integer reservedQty) {
        super("Cannot "+ action + " " + requestQty
                + " units for product " + productId
                + ". Only " + reservedQty + " units are reserved"
        );
    }
}
