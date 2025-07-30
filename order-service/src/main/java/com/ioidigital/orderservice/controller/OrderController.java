package com.ioidigital.orderservice.controller;

import com.ioidigital.orderservice.dto.OrderRequest;
import com.ioidigital.orderservice.dto.OrderResponse;
import com.ioidigital.orderservice.dto.OrderStatusUpdateRequest;
import com.ioidigital.orderservice.dto.PagedResponse;
import com.ioidigital.orderservice.entity.OrderStatus;
import com.ioidigital.orderservice.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/orders")
public class OrderController {

    private final OrderService orderService;

    @Autowired
    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping
    @Operation(summary = "Place a new order",
            description = "Creates a new order for a customer in a specific shop location.",
            responses = {
                    @ApiResponse(responseCode = "201", description = "Order placed successfully",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = OrderResponse.class))),
                    @ApiResponse(responseCode = "400", description = "Invalid order request")
            })
    public ResponseEntity<OrderResponse> createOrder(@Valid @RequestBody OrderRequest request) {
        OrderResponse response = orderService.createOrder(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping
    @Operation(summary = "Get all orders with pagination",
            description = "Retrieves a paginated list of all orders.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Orders retrieved successfully",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = PagedResponse.class)))
            })
    public ResponseEntity<PagedResponse<OrderResponse>> getAllOrders(
            @Parameter(description = "Page number (0-based)", example = "0")
            @RequestParam(name = "page", defaultValue = "0") int page,
            @Parameter(description = "Page size", example = "10")
            @RequestParam(name = "size", defaultValue = "10") int size,
            @Parameter(description = "Sort by field", example = "orderTime")
            @RequestParam(name = "sortBy", defaultValue = "orderTime") String sortBy,
            @Parameter(description = "Sort direction", example = "desc")
            @RequestParam(name = "sortDir", defaultValue = "desc") String sortDir) {

        Sort sort = sortDir.equalsIgnoreCase("desc") ?
                Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);

        PagedResponse<OrderResponse> response = orderService.getAllOrders(pageable);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{orderId}")
    @Operation(summary = "Get order by ID",
            description = "Retrieves details of a single order by its unique ID.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Order found",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = OrderResponse.class))),
                    @ApiResponse(responseCode = "404", description = "Order not found")
            })
    public ResponseEntity<OrderResponse> getOrderById(@PathVariable UUID orderId) {
        OrderResponse response = orderService.getOrderDetails(orderId);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{orderId}/status")
    @Operation(summary = "Update order status",
            description = "Updates the status of an order (e.g., CANCELLED, COMPLETED, IN_PROGRESS).",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Order status updated successfully",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = OrderResponse.class))),
                    @ApiResponse(responseCode = "404", description = "Order not found"),
                    @ApiResponse(responseCode = "400", description = "Invalid status update request")
            })
    public ResponseEntity<OrderResponse> updateOrderStatus(
            @PathVariable UUID orderId,
            @Valid @RequestBody OrderStatusUpdateRequest request) {
        OrderResponse response = orderService.updateOrderStatus(orderId, request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/customers/{customerId}")
    @Operation(summary = "Get orders by customer ID with pagination",
            description = "Retrieves a paginated list of orders placed by a specific customer.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Customer orders retrieved successfully",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = PagedResponse.class))),
                    @ApiResponse(responseCode = "404", description = "Customer not found")
            })
    public ResponseEntity<PagedResponse<OrderResponse>> getCustomerOrders(
            @PathVariable UUID customerId,
            @Parameter(description = "Page number (0-based)", example = "0")
            @RequestParam(name = "page", defaultValue = "0") int page,
            @Parameter(description = "Page size", example = "10")
            @RequestParam(name = "size", defaultValue = "10") int size,
            @Parameter(description = "Sort by field", example = "orderTime")
            @RequestParam(name = "sortBy", defaultValue = "orderTime") String sortBy,
            @Parameter(description = "Sort direction", example = "desc")
            @RequestParam(name = "sortDir", defaultValue = "desc") String sortDir) {

        Sort sort = sortDir.equalsIgnoreCase("desc") ?
                Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);

        PagedResponse<OrderResponse> response = orderService.getCustomerOrders(customerId, pageable);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/shops/{shopId}")
    @Operation(summary = "Get orders by shop ID with pagination",
            description = "Retrieves a paginated list of orders for a specific shop.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Shop orders retrieved successfully",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = PagedResponse.class))),
                    @ApiResponse(responseCode = "404", description = "Shop not found")
            })
    public ResponseEntity<PagedResponse<OrderResponse>> getShopOrders(
            @PathVariable UUID shopId,
            @Parameter(description = "Page number (0-based)", example = "0")
            @RequestParam(name = "page", defaultValue = "0") int page,
            @Parameter(description = "Page size", example = "10")
            @RequestParam(name = "size", defaultValue = "10") int size,
            @Parameter(description = "Sort by field", example = "orderTime")
            @RequestParam(name = "sortBy", defaultValue = "orderTime") String sortBy,
            @Parameter(description = "Sort direction", example = "desc")
            @RequestParam(name = "sortDir", defaultValue = "desc") String sortDir) {

        Sort sort = sortDir.equalsIgnoreCase("desc") ?
                Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);

        PagedResponse<OrderResponse> response = orderService.getOrdersByShop(shopId, pageable);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/status/{status}")
    @Operation(summary = "Get orders by status with pagination",
            description = "Retrieves a paginated list of orders filtered by status.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Orders by status retrieved successfully",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = PagedResponse.class)))
            })
    public ResponseEntity<PagedResponse<OrderResponse>> getOrdersByStatus(
            @PathVariable OrderStatus status,
            @Parameter(description = "Page number (0-based)", example = "0")
            @RequestParam(name = "page", defaultValue = "0") int page,
            @Parameter(description = "Page size", example = "10")
            @RequestParam(name = "size", defaultValue = "10") int size,
            @Parameter(description = "Sort by field", example = "orderTime")
            @RequestParam(name = "sortBy", defaultValue = "orderTime") String sortBy,
            @Parameter(description = "Sort direction", example = "desc")
            @RequestParam(name = "sortDir", defaultValue = "desc") String sortDir) {

        Sort sort = sortDir.equalsIgnoreCase("desc") ?
                Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);

        PagedResponse<OrderResponse> response = orderService.getOrdersByStatus(status, pageable);
        return ResponseEntity.ok(response);
    }

}
