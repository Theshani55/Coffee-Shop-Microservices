package com.ioidigital.orderservice.service;

import com.ioidigital.orderservice.dto.OrderItemDto;
import com.ioidigital.orderservice.dto.OrderRequest;
import com.ioidigital.orderservice.dto.OrderResponse;
import com.ioidigital.orderservice.dto.OrderStatusUpdateRequest;
import com.ioidigital.orderservice.entity.Order;
import com.ioidigital.orderservice.entity.OrderItem;
import com.ioidigital.orderservice.entity.OrderStatus;
import com.ioidigital.orderservice.exception.InvalidOrderException;
import com.ioidigital.orderservice.exception.ResourceNotFoundException;
import com.ioidigital.orderservice.repository.OrderItemRepository;
import com.ioidigital.orderservice.repository.OrderRepository;
import com.ioidigital.orderservice.service.external.MenuServiceClient;
import com.ioidigital.orderservice.service.external.ShopServiceClient;
import com.ioidigital.orderservice.service.impl.OrderServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SpringBootTest
class OrderServiceTest {

    @MockBean
    private OrderRepository orderRepository;

    @MockBean
    private OrderItemRepository orderItemRepository;

    @MockBean
    private MenuServiceClient menuServiceClient;

    @MockBean
    private ShopServiceClient shopServiceClient;

    private OrderService orderService;

    private UUID testOrderId;
    private UUID testCustomerId;
    private UUID testShopId;
    private UUID testMenuItemId;

    @BeforeEach
    void setUp() {
        orderService = new OrderServiceImpl(orderRepository, orderItemRepository, menuServiceClient, shopServiceClient);
        testOrderId = UUID.randomUUID();
        testCustomerId = UUID.randomUUID();
        testShopId = UUID.randomUUID();
        testMenuItemId = UUID.randomUUID();
    }

    @Test
    void createOrder_Success() {
        OrderItemDto itemDto = OrderItemDto.builder()
                .menuItemId(testMenuItemId)
                .quantity(2)
                .build();

        OrderRequest orderRequest = OrderRequest.builder()
                .shopId(testShopId)
                .customerId(testCustomerId)
                .items(Arrays.asList(itemDto))
                .build();

        when(shopServiceClient.doesShopExist(any())).thenReturn(true);
        when(menuServiceClient.getMenuItemPrice(any())).thenReturn(BigDecimal.valueOf(10.99));
        when(menuServiceClient.getMenuItemName(any())).thenReturn("Test Coffee");
        when(shopServiceClient.addOrderToQueue(any(), any())).thenReturn(1);
        when(orderRepository.save(any())).thenAnswer(invocation -> {
            Order order = invocation.getArgument(0);
            order.setId(testOrderId);
            return order;
        });

        OrderResponse response = orderService.createOrder(orderRequest);

        assertNotNull(response);
        assertEquals(testOrderId, response.getOrderId());
        assertEquals(testCustomerId, response.getCustomerId());
        assertEquals("PAID", response.getStatus());
        verify(orderRepository).save(any(Order.class));
        verify(orderItemRepository).save(any(OrderItem.class));
    }

    @Test
    void createOrder_InvalidShop_ThrowsException() {
        OrderRequest orderRequest = OrderRequest.builder()
                .shopId(testShopId)
                .customerId(testCustomerId)
                .items(new ArrayList<>())
                .build();

        when(shopServiceClient.doesShopExist(any())).thenReturn(false);
        when(menuServiceClient.getMenuItemPrice(any())).thenReturn(BigDecimal.valueOf(10.99));
        when(menuServiceClient.getMenuItemName(any())).thenReturn("Test Coffee");
        assertThrows(ResourceNotFoundException.class, () -> orderService.createOrder(orderRequest));
    }

    @Test
    void getOrder_ExistingOrder_Success() {
        // Given
        Order order = new Order();
        order.setId(testOrderId);
        order.setCustomerId(testCustomerId);
        order.setStatus(OrderStatus.PAID);

        when(orderRepository.findById(testOrderId)).thenReturn(Optional.of(order));

        // When
        OrderResponse response = orderService.getOrderDetails(testOrderId);

        // Then
        assertNotNull(response);
        assertEquals(testOrderId, response.getOrderId());
        assertEquals(testCustomerId, response.getCustomerId());
        assertEquals("PAID", response.getStatus());
    }

    @Test
    void getOrder_NonExistentOrder_ThrowsException() {
        // Given
        when(orderRepository.findById(any())).thenReturn(Optional.empty());

        // When & Then
        assertThrows(ResourceNotFoundException.class, () -> orderService.getOrderDetails(testOrderId));
    }

    @Test
    void updateOrderStatus_ValidTransition_Success() {
        // Given
        Order order = new Order();
        order.setId(testOrderId);
        order.setStatus(OrderStatus.PAID);

        OrderStatusUpdateRequest updateRequest = new OrderStatusUpdateRequest();
        updateRequest.setStatus(OrderStatus.PREPARING);

        when(orderRepository.findById(testOrderId)).thenReturn(Optional.of(order));
        when(orderRepository.save(any())).thenReturn(order);

        // When
        OrderResponse response = orderService.updateOrderStatus(testOrderId, updateRequest);

        // Then
        assertNotNull(response);
        assertEquals("PREPARING", response.getStatus());
    }

    @Test
    void updateOrderStatus_InvalidTransition_ThrowsException() {
        // Given
        Order order = new Order();
        order.setId(testOrderId);
        order.setStatus(OrderStatus.COMPLETED);

        OrderStatusUpdateRequest updateRequest = new OrderStatusUpdateRequest();
        updateRequest.setStatus(OrderStatus.PREPARING);

        when(orderRepository.findById(testOrderId)).thenReturn(Optional.of(order));

        // When & Then
        assertThrows(InvalidOrderException.class,
                () -> orderService.updateOrderStatus(testOrderId, updateRequest));
    }

    @Test
    void cancelOrder_PendingOrder_Success() {
        // Given
        Order order = new Order();
        order.setId(testOrderId);
        order.setStatus(OrderStatus.PENDING);

        OrderStatusUpdateRequest orderStatusUpdateRequest = OrderStatusUpdateRequest
                .builder()
                .status(OrderStatus.CANCELLED)
                .reason("Test cancellation")
                .build();

        when(orderRepository.findById(testOrderId)).thenReturn(Optional.of(order));
        when(orderRepository.save(any())).thenReturn(order);

        // When
        OrderResponse response = orderService.updateOrderStatus(testOrderId, orderStatusUpdateRequest);

        // Then
        assertNotNull(response);
        assertEquals("CANCELLED", response.getStatus());
    }

    @Test
    void cancelOrder_ReadyForPickupOrder_ThrowsException() {
        // Given
        Order order = new Order();
        order.setId(testOrderId);
        order.setStatus(OrderStatus.READY_FOR_PICKUP);

        OrderStatusUpdateRequest orderStatusUpdateRequest = OrderStatusUpdateRequest
                .builder()
                .status(OrderStatus.CANCELLED)
                .reason("Test cancellation")
                .build();

        when(orderRepository.findById(testOrderId)).thenReturn(Optional.of(order));

        // When & Then
        assertThrows(InvalidOrderException.class, () -> orderService.updateOrderStatus(testOrderId, orderStatusUpdateRequest));
    }

    @Test
    void getCustomerOrders_Success() {
        // Given
        Order order = new Order();
        order.setId(testOrderId);
        order.setCustomerId(testCustomerId);
        order.setStatus(OrderStatus.PREPARING);

        Page<Order> orderPage = new PageImpl<>(Arrays.asList(order));
        when(orderRepository.findByCustomerId(eq(testCustomerId), any())).thenReturn(orderPage);

        // When
        var response = orderService.getCustomerOrders(testCustomerId, PageRequest.of(0, 10));

        // Then
        assertNotNull(response);
        assertFalse(response.getContent().isEmpty());
        assertEquals(1, response.getContent().size());
        assertEquals(testOrderId, response.getContent().get(0).getOrderId());
    }
}
