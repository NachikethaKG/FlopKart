package com.flopkart.notification.service;

import com.flopkart.common.event.OrderPlacedEvent;
import com.flopkart.common.event.PaymentProcessedEvent;

public interface NotificationDispatcher {

    void dispatchOrderPlacedNotification(OrderPlacedEvent event);

    void dispatchPaymentSuccessNotification(PaymentProcessedEvent event);

    void dispatchPaymentFailedNotification(PaymentProcessedEvent event);
}
