package com.ioidigital.orderservice.dto;

import com.ioidigital.orderservice.entity.OrderStatus;
import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import jakarta.validation.constraints.NotNull;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderStatusUpdateRequest {
    @NotNull(message = "Order status is required")
    private OrderStatus status;

    private String reason; // Optional reason for status change
}
