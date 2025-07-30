package com.ioidigital.orderservice.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "order_items")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderItem {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @Column(name = "order_id", nullable = false)
    private UUID orderId;

    @Column(name = "menu_item_id", nullable = false)
    private UUID menuItemId;

    @Column(name = "quantity", nullable = false)
    private int quantity;

    @Column(name = "price_at_order", nullable = false, precision = 10, scale = 2)
    private BigDecimal unitPrice;

    @Column(name = "item_name", nullable = false)
    private String itemName;

    // Calculated field - not stored in database
    @Transient
    private BigDecimal itemTotalPrice;

    // Calculate total price when needed
    public BigDecimal getItemTotalPrice() {
        if (unitPrice != null && quantity > 0) {
            return unitPrice.multiply(BigDecimal.valueOf(quantity));
        }
        return BigDecimal.ZERO;
    }

}

