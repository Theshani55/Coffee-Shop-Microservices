package com.ioidigital.orderservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ioidigital.orderservice.dto.OrderItemDto;
import com.ioidigital.orderservice.dto.OrderRequest;
import com.ioidigital.orderservice.dto.OrderStatusUpdateRequest;
import com.ioidigital.orderservice.entity.Order;
import com.ioidigital.orderservice.entity.OrderItem;
import com.ioidigital.orderservice.entity.OrderStatus;
import com.ioidigital.orderservice.repository.OrderItemRepository;
import com.ioidigital.orderservice.repository.OrderRepository;
import com.ioidigital.orderservice.service.external.MenuServiceClient;
import com.ioidigital.orderservice.service.external.ShopServiceClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class OrderControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private OrderRepository orderRepository;

    @MockBean
    private OrderItemRepository orderItemRepository;

    @MockBean
    private MenuServiceClient menuServiceClient;

    @MockBean
    private ShopServiceClient shopServiceClient;

    private UUID orderId;
    private UUID customerId;
    private UUID shopId;
    private UUID menuItemId1;
    private UUID menuItemId2;

    @BeforeEach
    void setUp() {
        orderId = UUID.randomUUID();
        customerId = UUID.randomUUID();
        shopId = UUID.randomUUID();
        menuItemId1 = UUID.randomUUID();
        menuItemId2 = UUID.randomUUID();
    }

    @Test
    void createOrder_Success() throws Exception {
        // Given
        OrderItemDto item1 = OrderItemDto.builder()
                .menuItemId(menuItemId1)
                .quantity(1)
                .build();

        OrderItemDto item2 = OrderItemDto.builder()
                .menuItemId(menuItemId2)
                .quantity(2)
                .build();

        OrderRequest request = OrderRequest.builder()
                .shopId(shopId)
                .customerId(customerId)
                .items(Arrays.asList(item1, item2))
                .build();

        when(shopServiceClient.doesShopExist(any())).thenReturn(true);
        when(menuServiceClient.getMenuItemPrice(menuItemId1)).thenReturn(BigDecimal.valueOf(5.00));
        when(menuServiceClient.getMenuItemPrice(menuItemId2)).thenReturn(BigDecimal.valueOf(4.00));
        when(menuServiceClient.getMenuItemName(any())).thenReturn("Test Item");
        when(shopServiceClient.addOrderToQueue(any(), any())).thenReturn(1);

        when(orderRepository.save(any())).thenAnswer(invocation -> {
            Order order = invocation.getArgument(0);
            order.setId(orderId);
            return order;
        });

        // When & Then
        mockMvc.perform(post("/api/v1/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.orderId").exists())
                .andExpect(jsonPath("$.status").value(OrderStatus.PAID.name()));
    }

    @Test
    void getOrder_Success() throws Exception {
        // Given
        Order order = new Order();
        order.setId(orderId);
        order.setCustomerId(customerId);
        order.setShopId(shopId);
        order.setStatus(OrderStatus.PAID);
        order.setTotalAmount(BigDecimal.valueOf(13.00));
        order.setOrderTime(LocalDateTime.now());

        OrderItem orderItem = new OrderItem();
        orderItem.setId(UUID.randomUUID());
        orderItem.setOrderId(orderId);
        orderItem.setMenuItemId(menuItemId1);
        orderItem.setQuantity(2);
        orderItem.setUnitPrice(BigDecimal.valueOf(5.00));

        when(orderRepository.findById(orderId)).thenReturn(java.util.Optional.of(order));
        when(orderItemRepository.findByOrderId(orderId)).thenReturn(Arrays.asList(orderItem));

        // When & Then
        mockMvc.perform(get("/api/v1/orders/{orderId}", orderId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orderId").value(orderId.toString()))
                .andExpect(jsonPath("$.status").value(OrderStatus.PAID.name()))
                .andExpect(jsonPath("$.totalAmount").value(13.00));
    }

    @Test
    void updateOrderStatus_Success() throws Exception {
        // Given
        Order order = new Order();
        order.setId(orderId);
        order.setStatus(OrderStatus.PAID);

        OrderStatusUpdateRequest updateRequest = new OrderStatusUpdateRequest();
        updateRequest.setStatus(OrderStatus.PREPARING);
        updateRequest.setReason("Starting preparation");

        when(orderRepository.findById(orderId)).thenReturn(java.util.Optional.of(order));
        when(orderRepository.save(any())).thenReturn(order);

        // When & Then
        mockMvc.perform(patch("/api/v1/orders/{orderId}/status", orderId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(OrderStatus.PREPARING.name()));
    }

    @Test
    void getCustomerOrders_Success() throws Exception {
        // Given
        Order order = new Order();
        order.setId(orderId);
        order.setCustomerId(customerId);
        order.setStatus(OrderStatus.PAID);

        List<Order> orders = Arrays.asList(order);
        when(orderRepository.findByCustomerId(eq(customerId), any(Pageable.class)))
                .thenReturn(new PageImpl<>(orders));

        // When & Then
        mockMvc.perform(get("/api/v1/orders/customers/{customerId}", customerId)
                .param("page", "0")
                .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].orderId").value(orderId.toString()))
                .andExpect(jsonPath("$.content[0].customerId").value(customerId.toString()));
    }

    @Test
    void getOrder_NotFound() throws Exception {
        // Given
        when(orderRepository.findById(any())).thenReturn(java.util.Optional.empty());

        // When & Then
        mockMvc.perform(get("/api/v1/orders/{orderId}", UUID.randomUUID()))
                .andExpect(status().isNotFound());
    }

    @Test
    void createOrder_InvalidShop() throws Exception {
        OrderRequest request = OrderRequest.builder()
                .shopId(shopId)
                .customerId(customerId)
                .items(Arrays.asList(
                    OrderItemDto.builder()
                        .menuItemId(menuItemId1)
                        .quantity(1)
                        .build()
                ))
                .build();

        when(shopServiceClient.doesShopExist(any())).thenReturn(false);

        mockMvc.perform(post("/api/v1/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Shop not found with ID: " + shopId));
    }

    @Test
    void updateOrderStatus_InvalidTransition() throws Exception {
        // Given
        Order order = new Order();
        order.setId(orderId);
        order.setStatus(OrderStatus.COMPLETED);

        OrderStatusUpdateRequest updateRequest = new OrderStatusUpdateRequest();
        updateRequest.setStatus(OrderStatus.PREPARING);

        when(orderRepository.findById(orderId)).thenReturn(java.util.Optional.of(order));

        // When & Then
        mockMvc.perform(patch("/api/v1/orders/{orderId}/status", orderId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createOrder_EmptyItems() throws Exception {
        // Given
        OrderRequest request = OrderRequest.builder()
                .shopId(shopId)
                .customerId(customerId)
                .items(Arrays.asList())
                .build();

        // When & Then
        mockMvc.perform(post("/api/v1/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createOrder_InvalidMenuItem() throws Exception {
        // Given
        OrderItemDto item = OrderItemDto.builder()
                .menuItemId(menuItemId1)
                .quantity(1)
                .build();

        OrderRequest request = OrderRequest.builder()
                .shopId(shopId)
                .customerId(customerId)
                .items(Arrays.asList(item))
                .build();

        when(shopServiceClient.doesShopExist(any())).thenReturn(true);
        when(menuServiceClient.getMenuItemPrice(menuItemId1)).thenReturn(null);

        // When & Then
        mockMvc.perform(post("/api/v1/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Menu item not found or unavailable: " + menuItemId1));
    }

    @Test
    void updateOrderStatus_OrderNotFound() throws Exception {
        // Given
        OrderStatusUpdateRequest updateRequest = new OrderStatusUpdateRequest();
        updateRequest.setStatus(OrderStatus.PREPARING);
        updateRequest.setReason("Starting preparation");

        when(orderRepository.findById(any())).thenReturn(java.util.Optional.empty());

        // When & Then
        mockMvc.perform(patch("/api/v1/orders/{orderId}/status", UUID.randomUUID())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isNotFound());
    }

    @Test
    void getCustomerOrders_NoOrders() throws Exception {
        // Given
        when(orderRepository.findByCustomerId(eq(customerId), any(Pageable.class)))
                .thenReturn(new PageImpl<>(Arrays.asList()));

        // When & Then
        mockMvc.perform(get("/api/v1/orders/customers/{customerId}", customerId)
                .param("page", "0")
                .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content").isEmpty());
    }
}
