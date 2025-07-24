package com.ioidigital.orderservice.repository;


import com.ioidigital.orderservice.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface OrderRepository extends JpaRepository<Order, UUID> {
    List<Order> findByCustomerIdOrderByOrderTimeDesc(UUID customerId);
    List<Order> findByShopIdOrderByOrderTimeAsc(UUID shopId); // For shop app queue management
}