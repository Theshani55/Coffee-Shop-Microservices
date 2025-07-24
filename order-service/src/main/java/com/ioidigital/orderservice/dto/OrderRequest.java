package com.ioidigital.orderservice.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
@Builder
public class OrderRequest {
    @NotNull(message = "Shop ID cannot be null")
    private UUID shopId;
    @NotNull(message = "Customer ID cannot be null")
    private UUID customerId;
    @NotEmpty(message = "Order must contain at least one item")
    @Valid
    private List<OrderItemDto> items;

}