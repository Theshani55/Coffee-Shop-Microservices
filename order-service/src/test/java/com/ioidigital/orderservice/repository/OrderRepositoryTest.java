package com.ioidigital.orderservice.repository;

import com.ioidigital.orderservice.entity.Order;
import com.ioidigital.orderservice.entity.OrderStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Testcontainers
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE) // Don't replace with in-memory DB
public class OrderRepositoryTest {

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
    private OrderRepository orderRepository;
    @Autowired
    private OrderItemRepository orderItemRepository; // Also inject for cascading tests if needed

    private UUID customerId1;
    private UUID shopId1;
    private UUID shopId2;

    @BeforeEach
    void setUp() {
        orderRepository.deleteAll(); // Clean up before each test
        orderItemRepository.deleteAll();

        customerId1 = UUID.randomUUID();
        shopId1 = UUID.randomUUID();
        shopId2 = UUID.randomUUID();

        Order order1 = new Order();
        order1.setCustomerId(customerId1);
        order1.setShopId(shopId1);
        order1.setTotalAmount(BigDecimal.valueOf(10.50));
        order1.setStatus(OrderStatus.PAID);
        order1.setOrderTime(LocalDateTime.now().minusHours(1));
        orderRepository.save(order1);

        Order order2 = new Order();
        order2.setCustomerId(customerId1);
        order2.setShopId(shopId2);
        order2.setTotalAmount(BigDecimal.valueOf(15.00));
        order2.setStatus(OrderStatus.PENDING);
        order2.setOrderTime(LocalDateTime.now());
        orderRepository.save(order2);
    }

}
