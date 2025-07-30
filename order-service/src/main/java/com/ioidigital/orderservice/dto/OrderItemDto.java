package com.ioidigital.orderservice.dto;

import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class OrderItemDto {
    private UUID menuItemId;
    private int quantity;
}
