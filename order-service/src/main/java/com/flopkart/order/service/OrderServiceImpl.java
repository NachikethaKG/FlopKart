package com.flopkart.order.service;

import com.flopkart.common.dto.ApiResponse;
import com.flopkart.common.dto.cart.CartDto;
import com.flopkart.common.dto.order.CreateOrderRequest;
import com.flopkart.common.dto.order.OrderDto;
import com.flopkart.common.dto.order.OrderItemDto;
import com.flopkart.common.dto.product.StockItemDto;
import com.flopkart.common.dto.product.StockReservationRequest;
import com.flopkart.common.event.OrderPlacedEvent;
import com.flopkart.common.event.PaymentProcessedEvent;
import com.flopkart.common.event.StockReleaseEvent;
import com.flopkart.order.client.CartClient;
import com.flopkart.order.client.ProductClient;
import com.flopkart.order.entity.Order;
import com.flopkart.order.entity.OrderItem;
import com.flopkart.order.entity.OrderStatus;
import com.flopkart.order.event.OrderEventPublisher;
import com.flopkart.order.exception.EmptyCartException;
import com.flopkart.order.exception.ResourceNotFoundException;
import com.flopkart.order.exception.StockReservationException;
import com.flopkart.order.repository.OrderRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class OrderServiceImpl implements OrderService {

    private static final Logger log = LoggerFactory.getLogger(OrderServiceImpl.class);

    private final OrderRepository orderRepository;
    private final CartClient cartClient;
    private final ProductClient productClient;
    private final OrderEventPublisher orderEventPublisher;

    public OrderServiceImpl(OrderRepository orderRepository,
                            CartClient cartClient,
                            ProductClient productClient,
                            OrderEventPublisher orderEventPublisher) {
        this.orderRepository = orderRepository;
        this.cartClient = cartClient;
        this.productClient = productClient;
        this.orderEventPublisher = orderEventPublisher;
    }

    @Override
    @Transactional
    public OrderDto createOrder(String userIdStr, String userEmail, CreateOrderRequest request) {
        Long userId = Long.parseLong(userIdStr);
        log.info("Initiating checkout flow for user ID {}", userId);

        // 1. Fetch active cart via CartClient
        ApiResponse<CartDto> cartResponse = cartClient.getCart(userIdStr);
        if (cartResponse == null || !cartResponse.isSuccess() || cartResponse.getData() == null) {
            String msg = (cartResponse != null) ? cartResponse.getMessage() : "Failed to retrieve cart";
            throw new EmptyCartException("Cannot place order: " + msg);
        }

        CartDto cart = cartResponse.getData();
        if (cart.getItems() == null || cart.getItems().isEmpty()) {
            throw new EmptyCartException("Cannot place order: cart is empty");
        }

        // 2. Reserve inventory via ProductClient
        List<StockItemDto> stockItems = cart.getItems().stream()
                .map(item -> new StockItemDto(item.getProductId(), item.getProductName(), item.getQuantity()))
                .toList();

        StockReservationRequest reservationRequest = new StockReservationRequest(stockItems);
        ApiResponse<Void> reserveResponse = productClient.reserveStock(reservationRequest);

        if (reserveResponse == null || !reserveResponse.isSuccess()) {
            int status = (reserveResponse != null) ? reserveResponse.getStatus() : 409;
            String message = (reserveResponse != null) ? reserveResponse.getMessage() : "Stock reservation failed";
            log.warn("Stock reservation failed for user {}: {}", userId, message);
            throw new StockReservationException(message, status);
        }

        // 3. Persist order in PENDING state
        String orderNumber = "ORD-" + UUID.randomUUID().toString().replace("-", "").substring(0, 10).toUpperCase();
        BigDecimal totalAmount = cart.getTotalPrice() != null ? cart.getTotalPrice() : BigDecimal.ZERO;

        Order order = Order.builder()
                .orderNumber(orderNumber)
                .userId(userId)
                .totalAmount(totalAmount)
                .status(OrderStatus.PENDING)
                .shippingAddress(request != null ? request.getShippingAddress() : null)
                .build();

        for (var cartItem : cart.getItems()) {
            OrderItem orderItem = new OrderItem(
                    cartItem.getProductId(),
                    cartItem.getProductName(),
                    cartItem.getQuantity(),
                    cartItem.getPrice()
            );
            order.addItem(orderItem);
        }

        Order savedOrder = orderRepository.save(order);
        log.info("Order created with orderNumber {} in PENDING state", orderNumber);

        // 4. Publish OrderPlacedEvent to Kafka topic order-placed-topic
        String effectiveEmail = (userEmail != null && !userEmail.isBlank())
                ? userEmail
                : "user" + userId + "@flopkart.com";

        List<com.flopkart.common.event.OrderItemDto> eventItems = savedOrder.getItems().stream()
                .map(item -> new com.flopkart.common.event.OrderItemDto(
                        item.getProductId(),
                        item.getProductName(),
                        item.getQuantity(),
                        item.getPrice()
                ))
                .toList();

        OrderPlacedEvent event = new OrderPlacedEvent(
                savedOrder.getOrderNumber(),
                String.valueOf(savedOrder.getUserId()),
                effectiveEmail,
                savedOrder.getTotalAmount(),
                eventItems,
                LocalDateTime.now()
        );

        orderEventPublisher.publishOrderPlaced(event);

        return mapToDto(savedOrder);
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderDto> getUserOrders(String userIdStr) {
        Long userId = Long.parseLong(userIdStr);
        List<Order> orders = orderRepository.findByUserIdOrderByCreatedAtDesc(userId);
        return orders.stream().map(this::mapToDto).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public OrderDto getOrderByOrderNumber(String orderNumber) {
        Order order = orderRepository.findByOrderNumber(orderNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with order number: " + orderNumber));
        return mapToDto(order);
    }

    @Override
    @Transactional
    public void processPaymentResult(PaymentProcessedEvent event) {
        if (event == null || event.orderNumber() == null) {
            log.warn("Received empty PaymentProcessedEvent, skipping");
            return;
        }

        log.info("Processing PaymentProcessedEvent for order {}: status={}", event.orderNumber(), event.status());
        Optional<Order> orderOpt = orderRepository.findByOrderNumber(event.orderNumber());
        if (orderOpt.isEmpty()) {
            log.warn("Order not found for orderNumber: {}", event.orderNumber());
            return;
        }

        Order order = orderOpt.get();

        if ("SUCCESS".equalsIgnoreCase(event.status())) {
            order.setStatus(OrderStatus.CONFIRMED);
            orderRepository.save(order);
            log.info("Order {} transitioned to CONFIRMED", order.getOrderNumber());

            // Clear user's cart
            try {
                cartClient.clearCart(String.valueOf(order.getUserId()));
                log.info("Cleared cart for user {}", order.getUserId());
            } catch (Exception e) {
                log.error("Failed to clear cart for user {}: {}", order.getUserId(), e.getMessage());
            }
        } else {
            order.setStatus(OrderStatus.CANCELLED);
            orderRepository.save(order);
            log.info("Order {} transitioned to CANCELLED. Failure reason: {}", order.getOrderNumber(), event.failureReason());

            // Compensating action: publish StockReleaseEvent to stock-release-topic
            List<com.flopkart.common.event.OrderItemDto> releaseItems = order.getItems().stream()
                    .map(item -> new com.flopkart.common.event.OrderItemDto(
                            item.getProductId(),
                            item.getProductName(),
                            item.getQuantity(),
                            item.getPrice()
                    ))
                    .toList();

            StockReleaseEvent releaseEvent = new StockReleaseEvent(
                    order.getOrderNumber(),
                    releaseItems,
                    LocalDateTime.now()
            );

            orderEventPublisher.publishStockRelease(releaseEvent);
        }
    }

    public OrderDto mapToDto(Order order) {
        List<OrderItemDto> itemDtos = (order.getItems() != null)
                ? order.getItems().stream()
                .map(item -> OrderItemDto.builder()
                        .productId(item.getProductId())
                        .productName(item.getProductName())
                        .quantity(item.getQuantity())
                        .unitPrice(item.getPrice())
                        .subtotal(item.getSubtotal())
                        .build())
                .toList()
                : List.of();

        return OrderDto.builder()
                .id(order.getId())
                .orderNumber(order.getOrderNumber())
                .userId(order.getUserId())
                .totalAmount(order.getTotalAmount())
                .status(order.getStatus() != null ? order.getStatus().name() : OrderStatus.PENDING.name())
                .shippingAddress(order.getShippingAddress())
                .items(itemDtos)
                .createdAt(order.getCreatedAt())
                .updatedAt(order.getUpdatedAt())
                .build();
    }
}
