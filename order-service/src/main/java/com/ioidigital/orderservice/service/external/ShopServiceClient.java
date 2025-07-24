package com.ioidigital.orderservice.service.external;

import org.springframework.stereotype.Service;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class ShopServiceClient {
    // Mock data for demonstration - assuming these shops exist and are valid
    private final ConcurrentHashMap<UUID, AtomicInteger> shopQueues = new ConcurrentHashMap<>();

    public ShopServiceClient() {
        // Sample shop IDs
        UUID shop1 = UUID.fromString("b0000000-0000-0000-0000-000000000001");
        UUID shop2 = UUID.fromString("b0000000-0000-0000-0000-000000000002");
        shopQueues.put(shop1, new AtomicInteger(0)); // Initialize queue size
        shopQueues.put(shop2, new AtomicInteger(0));
    }

    // Simulate API call to Shop Service
    public boolean doesShopExist(UUID shopId) {
        System.out.println("ShopServiceClient: Checking if shop exists " + shopId);
        return shopQueues.containsKey(shopId);
    }

    // Simulate API call to Shop Service to add to queue
    public Integer addOrderToQueue(UUID shopId, UUID orderId) {
        System.out.println("ShopServiceClient: Adding order " + orderId + " to shop " + shopId + " queue.");
        AtomicInteger currentQueueSize = shopQueues.get(shopId);
        if (currentQueueSize != null) {
            return currentQueueSize.incrementAndGet(); // Simple increment for demo
        }
        return null; // Shop not found
    }

    // Simulate API call to Shop Service to remove from queue
    public void removeOrderFromQueue(UUID shopId, UUID orderId) {
        System.out.println("ShopServiceClient: Removing order " + orderId + " from shop " + shopId + " queue.");
        AtomicInteger currentQueueSize = shopQueues.get(shopId);
        if (currentQueueSize != null && currentQueueSize.get() > 0) {
            currentQueueSize.decrementAndGet();
        }
    }
}
