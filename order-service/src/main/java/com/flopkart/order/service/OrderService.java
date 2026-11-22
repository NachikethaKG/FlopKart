package com.flopkart.order.service;

import com.flopkart.common.dto.order.CreateOrderRequest;
import com.flopkart.common.dto.order.OrderDto;
import com.flopkart.common.event.PaymentProcessedEvent;

import java.util.List;

public interface OrderService {

    OrderDto createOrder(String userIdStr, String userEmail, CreateOrderRequest request);

    List<OrderDto> getUserOrders(String userIdStr);

    OrderDto getOrderByOrderNumber(String orderNumber);

    void processPaymentResult(PaymentProcessedEvent event);
}
