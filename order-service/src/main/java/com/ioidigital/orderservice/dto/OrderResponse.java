package com.ioidigital.orderservice.dto;

import com.ioidigital.orderservice.entity.Order;
import com.ioidigital.orderservice.entity.OrderItem;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Data
public class OrderResponse {
    private UUID orderId;
    private UUID customerId;
    private UUID shopId;
    private LocalDateTime orderTime;
    private String status;
    private BigDecimal totalAmount;
    private Integer queuePosition;
    private LocalDateTime estimatedWaitingTime;
    private List<OrderItemResponse> items;

    public static OrderResponse fromOrderEntityToOrderResponse(Order order, List<OrderItem> orderItems) {
        OrderResponse response = new OrderResponse();
        response.setOrderId(order.getId());
        response.setCustomerId(order.getCustomerId());
        response.setShopId(order.getShopId());
        response.setOrderTime(order.getOrderTime());
        response.setStatus(order.getStatus().name());
        response.setTotalAmount(order.getTotalAmount());
        response.setQueuePosition(order.getQueuePosition());
        response.setEstimatedWaitingTime(order.getEstimatedWaitingTime());
        response.setItems(orderItems.stream().map(OrderItemResponse::from).collect(Collectors.toList()));
        return response;
    }
}