package com.ioidigital.orderservice.service.impl;

import com.ioidigital.orderservice.dto.OrderItemDto;
import com.ioidigital.orderservice.entity.Order;
import com.ioidigital.orderservice.entity.OrderItem;
import com.ioidigital.orderservice.entity.OrderStatus;
import com.ioidigital.orderservice.exception.InvalidOrderException;
import com.ioidigital.orderservice.service.OrderService;

import com.ioidigital.orderservice.dto.OrderRequest;
import com.ioidigital.orderservice.dto.OrderResponse;
import com.ioidigital.orderservice.exception.ResourceNotFoundException;
import com.ioidigital.orderservice.repository.OrderItemRepository;
import com.ioidigital.orderservice.repository.OrderRepository;
import com.ioidigital.orderservice.service.external.MenuServiceClient;
import com.ioidigital.orderservice.service.external.ShopServiceClient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final MenuServiceClient menuServiceClient; // For menu details
    private final ShopServiceClient shopServiceClient; // For shop details and queue management

    @Transactional
    public OrderResponse createOrder(OrderRequest request) {
        // 1. Mocked Shop and Customer existence validation
        // This should validate in Shop services.
        if (!shopServiceClient.doesShopExist(request.getShopId())) {
            throw new ResourceNotFoundException("Shop not found with ID: " + request.getShopId());
        }

        // Assuming customer validation happens at API Gateway via JWT or User Service.
        // if (!userServiceClient.doesCustomerExist(request.getCustomerId())) {
        //      throw new ResourceNotFoundException("Customer not found with ID: " + request.getCustomerId());
        // }


        // 2. Validate menu items and calculate total item amount
        BigDecimal totalAmount = BigDecimal.ZERO;
        List<OrderItem> orderItems = new ArrayList<>();

        for (OrderItemDto itemDto : request.getItems()) {
            // Mock call to Menu Service to get price and item name
            BigDecimal itemPrice = menuServiceClient.getMenuItemPrice(itemDto.getMenuItemId());
            String itemName = menuServiceClient.getMenuItemName(itemDto.getMenuItemId());
            if (itemPrice == null || itemName == null) {
                throw new InvalidOrderException("Menu item not found or unavailable: " + itemDto.getMenuItemId());
            }
            if (itemDto.getQuantity() <= 0) {
                throw new InvalidOrderException("Quantity for item " + itemName + " must be positive.");
            }

            BigDecimal itemTotalPrice = itemPrice.multiply(BigDecimal.valueOf(itemDto.getQuantity()));
            totalAmount = totalAmount.add(itemTotalPrice);

            OrderItem orderItem = OrderItem.builder()
                    .menuItemId(itemDto.getMenuItemId())
                    .quantity(itemDto.getQuantity())
                    .unitPrice(itemPrice)
                    .itemTotalPrice(itemPrice.multiply(BigDecimal.valueOf(itemDto.getQuantity())))
                    .itemName(itemName)
                    .createdAt(LocalDateTime.now())
                    .build();

        }

        if (orderItems.isEmpty()) {
            throw new InvalidOrderException("Order must contain at least one valid item.");
        }

        // 3. Create and Save Order
        Order order = new Order();
        order.setCustomerId(request.getCustomerId());
        order.setShopId(request.getShopId());
        order.setStatus(OrderStatus.PAID);
        order.setTotalAmount(totalAmount);
        order.setOrderTime(LocalDateTime.now());

        // 4. Simulate adding to queue and getting position/ETA
        Integer queuePosition = shopServiceClient.addOrderToQueue(request.getShopId(), order.getId());
        LocalDateTime estimatedPickupTime = LocalDateTime.now().plusMinutes(queuePosition * 2); // Simple estimation

        order.setQueuePosition(queuePosition);
        order.setEstimatedWaitingTime(estimatedPickupTime);

        Order savedOrder = orderRepository.save(order);

        // 5. Save Order Items
        for (OrderItem item : orderItems) {
            item.setOrderId(savedOrder.getId());
            orderItemRepository.save(item);
        }

        // TODO: Publish an event (e.g., to Kafka) for Notification Service to send confirmation

        return OrderResponse.fromOrderEntityToOrderResponse(savedOrder, orderItems);
    }

    public OrderResponse getOrderDetails(UUID orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with ID: " + orderId));
        List<OrderItem> items = orderItemRepository.findByOrderId(orderId);
        return OrderResponse.fromOrderEntityToOrderResponse(order, items);
    }

    @Transactional
    public OrderResponse cancelOrder(UUID orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with ID: " + orderId));

        if (order.getStatus() == OrderStatus.COMPLETED || order.getStatus() == OrderStatus.CANCELLED) {
            throw new InvalidOrderException("Cannot cancel an order that is already " + order.getStatus().name());
        }

        // Mocked Remove order from shop queue
        shopServiceClient.removeOrderFromQueue(order.getShopId(), order.getId());


        order.setStatus(OrderStatus.CANCELLED);
        orderRepository.save(order);

        List<OrderItem> items = orderItemRepository.findByOrderId(orderId);

        // TODO: Publish an event for Notification Service to send cancellation notification

        return OrderResponse.fromOrderEntityToOrderResponse(order, items);
    }

    //Get all orders for a customer
    public List<OrderResponse> getCustomerOrders(UUID customerId) {
        List<Order> orders = orderRepository.findByCustomerIdOrderByOrderTimeDesc(customerId);
        return orders.stream()
                .map(order -> OrderResponse.fromOrderEntityToOrderResponse(order, orderItemRepository.findByOrderId(order.getId())))
                .collect(Collectors.toList());
    }
}
