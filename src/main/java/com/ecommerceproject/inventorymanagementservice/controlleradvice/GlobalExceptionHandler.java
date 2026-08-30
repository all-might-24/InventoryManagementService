package com.ecommerceproject.inventorymanagementservice.controlleradvice;

import com.ecommerceproject.inventorymanagementservice.dtos.responsedto.ExceptionDto;
import com.ecommerceproject.inventorymanagementservice.exceptions.InsufficientStockException;
import com.ecommerceproject.inventorymanagementservice.exceptions.InvalidInventoryOperationException;
import com.ecommerceproject.inventorymanagementservice.exceptions.InventoryNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.time.LocalDateTime;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(InventoryNotFoundException.class)
    public ResponseEntity<ExceptionDto> handleInventoryNotFoundException(InventoryNotFoundException e) {
        int status = HttpStatus.NOT_FOUND.value();
        ExceptionDto dto = createExceptionDto(status, e.getMessage());
        return ResponseEntity
                .status(status)
                .body(dto);
    }

    @ExceptionHandler(InsufficientStockException.class)
    public ResponseEntity<ExceptionDto> handleInsufficientStockException(InsufficientStockException e) {
        int status = HttpStatus.BAD_REQUEST.value();
        ExceptionDto dto = createExceptionDto(status, e.getMessage());
        return ResponseEntity
                .status(status)
                .body(dto);
    }

    @ExceptionHandler(InvalidInventoryOperationException.class)
    public ResponseEntity<ExceptionDto> handleInvalidInventoryOperationException(InvalidInventoryOperationException e) {
        int status = HttpStatus.BAD_REQUEST.value();
        ExceptionDto dto = createExceptionDto(status, e.getMessage());
        return ResponseEntity
                .status(status)
                .body(dto);
    }


    private ExceptionDto createExceptionDto(int status, String message) {
        ExceptionDto dto = new ExceptionDto();

        dto.setStatus(status);
        dto.setMessage(message);
        dto.setTimeStamp(LocalDateTime.now());

        return dto;
    }
}
