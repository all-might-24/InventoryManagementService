package com.ecommerceproject.inventorymanagementservice.dtos.responsedto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.Map;

@Getter
@Setter
public class ExceptionDto {
    private int status;
    private String message;
    private LocalDateTime timeStamp;
    private Map<String, String> errors;
}
