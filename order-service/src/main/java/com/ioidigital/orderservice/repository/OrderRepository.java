package com.ioidigital.orderservice.repository;


import com.ioidigital.orderservice.entity.Order;
import com.ioidigital.orderservice.entity.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface OrderRepository extends JpaRepository<Order, UUID> {

    Page<Order> findByCustomerId(UUID customerId, Pageable pageable);
    Page<Order> findByShopId(UUID shopId, Pageable pageable);
    Page<Order> findByStatus(OrderStatus status, Pageable pageable);

}