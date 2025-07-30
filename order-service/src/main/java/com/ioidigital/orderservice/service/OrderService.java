package com.ioidigital.orderservice.service;


import com.ioidigital.orderservice.dto.OrderRequest;
import com.ioidigital.orderservice.dto.OrderResponse;
import com.ioidigital.orderservice.dto.OrderStatusUpdateRequest;
import com.ioidigital.orderservice.dto.PagedResponse;

import com.ioidigital.orderservice.entity.OrderStatus;
import org.springframework.data.domain.Pageable;
import java.util.List;
import java.util.UUID;


public interface OrderService {

    OrderResponse createOrder(OrderRequest request);

    OrderResponse getOrderDetails(UUID orderId);

    OrderResponse updateOrderStatus(UUID orderId, OrderStatusUpdateRequest request);

    PagedResponse<OrderResponse> getCustomerOrders(UUID customerId, Pageable pageable);

    PagedResponse<OrderResponse> getAllOrders(Pageable pageable);

    PagedResponse<OrderResponse> getOrdersByShop(UUID shopId, Pageable pageable);

    PagedResponse<OrderResponse> getOrdersByStatus(OrderStatus status, Pageable pageable);

}
