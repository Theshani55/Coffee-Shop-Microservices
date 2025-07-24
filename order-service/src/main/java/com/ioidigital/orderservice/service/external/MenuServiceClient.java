package com.ioidigital.orderservice.service.external;

import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
public class MenuServiceClient {
    // Mock data for demonstration
    private final Map<UUID, BigDecimal> prices = new HashMap<>();
    private final Map<UUID, String> names = new HashMap<>();

    public MenuServiceClient() {
        // Sample menu items
        UUID latteId = UUID.fromString("a0000000-0000-0000-0000-000000000001");
        UUID cappuccinoId = UUID.fromString("a0000000-0000-0000-0000-000000000002");
        UUID espressoId = UUID.fromString("a0000000-0000-0000-0000-000000000003");
        UUID croissantId = UUID.fromString("a0000000-0000-0000-0000-000000000004");

        prices.put(latteId, BigDecimal.valueOf(4.50));
        names.put(latteId, "Latte");
        prices.put(cappuccinoId, BigDecimal.valueOf(4.00));
        names.put(cappuccinoId, "Cappuccino");
        prices.put(espressoId, BigDecimal.valueOf(3.00));
        names.put(espressoId, "Espresso");
        prices.put(croissantId, BigDecimal.valueOf(3.20));
        names.put(croissantId, "Croissant");
    }

    // Simulate API call to Product Service
    public BigDecimal getMenuItemPrice(UUID menuItemId) {
        System.out.println("ProductServiceClient: Getting price for item " + menuItemId);
        return prices.get(menuItemId); // Returns null if not found
    }

    // Simulate API call to Product Service
    public String getMenuItemName(UUID menuItemId) {
        System.out.println("ProductServiceClient: Getting name for item " + menuItemId);
        return names.get(menuItemId); // Returns null if not found
    }
}
