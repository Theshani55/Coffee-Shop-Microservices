package com.ioidigital.orderservice.service;

import com.ioidigital.orderservice.dto.OrderItemDto;
import com.ioidigital.orderservice.dto.OrderRequest;
import com.ioidigital.orderservice.dto.OrderResponse;
import com.ioidigital.orderservice.entity.Order;
import com.ioidigital.orderservice.entity.OrderStatus;
import com.ioidigital.orderservice.repository.OrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class OrderServiceTest {

    @Autowired
    private OrderService orderService;

    @Autowired
    private OrderRepository orderRepository;

    @BeforeEach
    void setUp() {
        orderRepository.deleteAll();
    }

    @Test
    void shouldPlaceNewOrder() {
        // Given
        UUID customerId = UUID.randomUUID();
        UUID shopId = UUID.randomUUID();
        List<OrderItemDto> items = new ArrayList<>();
        items.add(new OrderItemDto());

        OrderRequest orderRequest = OrderRequest.builder()
                .shopId(shopId).customerId(customerId).items(items).build();

        // When
        OrderResponse orderResponse = orderService.createOrder(orderRequest);

        // Then
        assertNotNull(orderResponse);
        assertNotNull(orderResponse.getOrderId());
        assertEquals(customerId, orderResponse.getCustomerId());
        assertEquals(OrderStatus.PENDING, orderResponse.getStatus());
        assertNotNull(orderResponse.getEstimatedWaitingTime());

        // Verify it's saved in the database
        Order retrievedOrder = orderRepository.findById(orderResponse.getOrderId()).orElse(null);
        assertNotNull(retrievedOrder);
        assertEquals(orderResponse.getOrderId(), retrievedOrder.getId());
    }


    @Test
    void shouldThrowOrderNotFoundExceptionForNonExistentOrder() {
        // TODO
    }

    @Test
    void shouldThrowOrderNotFoundExceptionOnCancelNonExistentOrder() {
        //TODO
    }

}
