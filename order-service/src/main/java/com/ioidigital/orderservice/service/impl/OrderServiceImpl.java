package com.ioidigital.orderservice.service.impl;

import com.ioidigital.orderservice.dto.OrderItemDto;
import com.ioidigital.orderservice.dto.OrderStatusUpdateRequest;
import com.ioidigital.orderservice.dto.PagedResponse;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
                    .itemName(itemName)
                    .build();
            orderItems.add(orderItem);

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

    @Override
    public OrderResponse updateOrderStatus(UUID orderId, OrderStatusUpdateRequest request) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with ID: " + orderId));

        // Validate status transition
        validateStatusTransition(order.getStatus(), request.getStatus());

        OrderStatus oldStatus = order.getStatus();
        order.setStatus(request.getStatus());

        // Handle specific status changes
        if (request.getStatus() == OrderStatus.CANCELLED && oldStatus != OrderStatus.CANCELLED) {
            // Remove from queue if being cancelled
            shopServiceClient.removeOrderFromQueue(order.getShopId(), order.getId());
        }

        orderRepository.save(order);
        List<OrderItem> items = orderItemRepository.findByOrderId(orderId);

        // TODO: Publish status change event for notifications

        return OrderResponse.fromOrderEntityToOrderResponse(order, items);
    }

    @Override
    public PagedResponse<OrderResponse> getCustomerOrders(UUID customerId, Pageable pageable) {
        Page<Order> orderPage = orderRepository.findByCustomerId(customerId, pageable);
        return buildPagedResponse(orderPage);
    }

    @Override
    public PagedResponse<OrderResponse> getAllOrders(Pageable pageable) {
        Page<Order> orderPage = orderRepository.findAll(pageable);
        return buildPagedResponse(orderPage);
    }

    @Override
    public PagedResponse<OrderResponse> getOrdersByShop(UUID shopId, Pageable pageable) {
        Page<Order> orderPage = orderRepository.findByShopId(shopId, pageable);
        return buildPagedResponse(orderPage);
    }

    @Override
    public PagedResponse<OrderResponse> getOrdersByStatus(OrderStatus status, Pageable pageable) {
        Page<Order> orderPage = orderRepository.findByStatus(status, pageable);
        return buildPagedResponse(orderPage);
    }

    // Build paginated response
    private PagedResponse<OrderResponse> buildPagedResponse(Page<Order> orderPage) {
        List<OrderResponse> orderResponses = orderPage.getContent().stream()
                .map(order -> {
                    List<OrderItem> items = orderItemRepository.findByOrderId(order.getId());
                    return OrderResponse.fromOrderEntityToOrderResponse(order, items);
                })
                .collect(Collectors.toList());

        return PagedResponse.<OrderResponse>builder()
                .content(orderResponses)
                .page(orderPage.getNumber())
                .size(orderPage.getSize())
                .totalElements(orderPage.getTotalElements())
                .totalPages(orderPage.getTotalPages())
                .first(orderPage.isFirst())
                .last(orderPage.isLast())
                .hasNext(orderPage.hasNext())
                .hasPrevious(orderPage.hasPrevious())
                .build();
    }

    private void validateStatusTransition(OrderStatus currentStatus, OrderStatus newStatus) {
        // Define allowed transitions
        switch (currentStatus) {
            case PAID:
                if (newStatus != OrderStatus.PREPARING && newStatus != OrderStatus.CANCELLED) {
                    throw new InvalidOrderException("Cannot change status from PAID to " + newStatus);
                }
                break;
            case PREPARING:
                if (newStatus != OrderStatus.READY_FOR_PICKUP && newStatus != OrderStatus.CANCELLED) {
                    throw new InvalidOrderException("Cannot change status from PREPARING to " + newStatus);
                }
                break;
            case READY_FOR_PICKUP:
                if (newStatus != OrderStatus.COMPLETED && newStatus != OrderStatus.CANCELLED) {
                    throw new InvalidOrderException("Cannot change status from READY_FOR_PICKUP to " + newStatus);
                }
                break;
            case COMPLETED:
                throw new InvalidOrderException("Cannot change status of a completed order");
            case CANCELLED:
                throw new InvalidOrderException("Cannot change status of a cancelled order");
            default:
                throw new InvalidOrderException("Unknown order status: " + currentStatus);
        }
    }

}
