package com.ioidigital.orderservice.entity;

public enum OrderStatus {
    PENDING,        // Initial state after order placement
    PAID,           // Payment successful
    PREPARING,      // Shop started preparing
    READY_FOR_PICKUP, // Order is ready
    COMPLETED,      // Customer picked up
    CANCELLED       // Order cancelled by customer or shop
}
