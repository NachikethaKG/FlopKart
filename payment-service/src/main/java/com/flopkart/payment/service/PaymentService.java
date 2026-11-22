package com.flopkart.payment.service;

import com.flopkart.common.event.OrderPlacedEvent;
import com.flopkart.payment.dto.PaymentResponse;

public interface PaymentService {

    PaymentResponse processOrderPayment(OrderPlacedEvent event);

    PaymentResponse getPaymentByOrderNumber(String orderNumber);
}
