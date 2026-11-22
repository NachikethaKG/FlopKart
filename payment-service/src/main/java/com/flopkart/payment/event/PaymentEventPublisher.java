package com.flopkart.payment.event;

import com.flopkart.common.event.PaymentProcessedEvent;

public interface PaymentEventPublisher {

    void publishPaymentProcessed(PaymentProcessedEvent event);
}
