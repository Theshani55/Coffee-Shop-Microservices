package com.ioidigital.orderservice.service;


import com.ioidigital.orderservice.dto.OrderRequest;
import com.ioidigital.orderservice.dto.OrderResponse;

import java.util.List;
import java.util.UUID;


public interface OrderService {

    public OrderResponse createOrder(OrderRequest request);
    public OrderResponse getOrderDetails(UUID orderId);
    public OrderResponse cancelOrder(UUID orderId);
    public List<OrderResponse> getCustomerOrders(UUID customerId);

}
