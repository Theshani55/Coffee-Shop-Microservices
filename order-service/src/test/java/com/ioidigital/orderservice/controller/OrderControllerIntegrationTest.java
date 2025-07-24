package com.ioidigital.orderservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ioidigital.orderservice.dto.OrderRequest;
import com.ioidigital.orderservice.dto.OrderItemDto;
import com.ioidigital.orderservice.entity.Order;
import com.ioidigital.orderservice.entity.OrderStatus;
import com.ioidigital.orderservice.repository.OrderRepository;
import com.ioidigital.orderservice.service.external.MenuServiceClient;
import com.ioidigital.orderservice.service.external.ShopServiceClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.assertj.core.api.Assertions.assertThat;


@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
@Transactional // Rollback changes after each test
public class OrderControllerIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.liquibase.change-log", () -> "classpath:db/changelog/db.changelog-master.yaml");
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private OrderRepository orderRepository;

    @Mock
    private MenuServiceClient menuServiceClient;
    @Mock
    private ShopServiceClient shopServiceClient;

    private UUID customerId;
    private UUID shopId;
    private UUID menuItemId1;
    private UUID menuItemId2;

    @BeforeEach
    void setUp() {
        customerId = UUID.randomUUID();
        shopId = UUID.fromString("b0000000-0000-0000-0000-000000000001"); // Use a known mock shop ID
        menuItemId1 = UUID.fromString("a0000000-0000-0000-0000-000000000001"); // Mock latte
        menuItemId2 = UUID.fromString("a0000000-0000-0000-0000-000000000002"); // Mock cappuccino

        // Mock external service behavior
        when(shopServiceClient.doesShopExist(shopId)).thenReturn(true);
        when(menuServiceClient.getMenuItemPrice(menuItemId1)).thenReturn(BigDecimal.valueOf(4.50));
        when(menuServiceClient.getMenuItemName(menuItemId1)).thenReturn("Latte");
        when(menuServiceClient.getMenuItemPrice(menuItemId2)).thenReturn(BigDecimal.valueOf(4.00));
        when(menuServiceClient.getMenuItemName(menuItemId2)).thenReturn("Cappuccino");
        when(shopServiceClient.addOrderToQueue(any(UUID.class), any(UUID.class))).thenReturn(1); // Mock queue position

    }

    @Test
    void shouldCreateOrderSuccessfully() throws Exception {
        OrderItemDto item1 = new OrderItemDto();
        item1.setMenuItemId(menuItemId1);
        item1.setQuantity(2);

        OrderItemDto item2 = new OrderItemDto();
        item2.setMenuItemId(menuItemId2);
        item2.setQuantity(1);

        OrderRequest request = OrderRequest
                .builder().shopId(shopId)
                .customerId(customerId)
                .items(Arrays.asList(item1, item2)).build();

        mockMvc.perform(post("/api/v1/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.orderId").exists())
                .andExpect(jsonPath("$.status").value(OrderStatus.PAID.name()))
                .andExpect(jsonPath("$.totalAmount").value(13.00))
                .andExpect(jsonPath("$.items[0].menuItemId").value(item1.getMenuItemId().toString()))
                .andExpect(jsonPath("$.items[1].quantity").value(item2.getQuantity()));
    }

    @Test
    void shouldGetOrderDetails() throws Exception {
        Order order = new Order();
        order.setCustomerId(customerId);
        order.setShopId(shopId);
        order.setStatus(OrderStatus.PAID);
        order.setTotalAmount(BigDecimal.valueOf(10.00));
        order.setQueuePosition(3);
        order.setOrderTime(LocalDateTime.now().plusMinutes(10));
        orderRepository.save(order);

        mockMvc.perform(get("/api/v1/orders/" + order.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orderId").value(order.getId().toString()))
                .andExpect(jsonPath("$.status").value(OrderStatus.PAID.name()))
                .andExpect(jsonPath("$.queuePosition").value(3));
    }

    @Test
    void shouldCancelOrderSuccessfully() throws Exception {
        Order order = new Order();
        order.setCustomerId(customerId);
        order.setShopId(shopId);
        order.setStatus(OrderStatus.PAID);
        order.setTotalAmount(BigDecimal.valueOf(10.00));
        orderRepository.save(order);

        mockMvc.perform(post("/api/v1/orders/" + order.getId() + "/cancel"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orderId").value(order.getId().toString()))
                .andExpect(jsonPath("$.status").value(OrderStatus.CANCELLED.name()));

        Optional<Order> cancelledOrder = orderRepository.findById(order.getId());
        assertThat(cancelledOrder).isPresent();
        assertThat(cancelledOrder.get().getStatus()).isEqualTo(OrderStatus.CANCELLED);
    }
}
