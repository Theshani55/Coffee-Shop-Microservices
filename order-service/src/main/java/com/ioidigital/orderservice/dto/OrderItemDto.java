package com.ioidigital.orderservice.dto;

import lombok.Data;

import java.util.UUID;

@Data
public class OrderItemDto {
    private UUID menuItemId;
    private int quantity;
}
