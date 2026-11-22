package com.flopkart.payment.service;

import com.flopkart.common.event.OrderPlacedEvent;
import com.flopkart.common.event.PaymentProcessedEvent;
import com.flopkart.payment.dto.PaymentResponse;
import com.flopkart.payment.entity.Payment;
import com.flopkart.payment.entity.PaymentStatus;
import com.flopkart.payment.event.PaymentEventPublisher;
import com.flopkart.payment.exception.ResourceNotFoundException;
import com.flopkart.payment.repository.PaymentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
public class PaymentServiceImpl implements PaymentService {

    private static final Logger log = LoggerFactory.getLogger(PaymentServiceImpl.class);

    private final PaymentRepository paymentRepository;
    private final PaymentEventPublisher paymentEventPublisher;

    @Value("${payment.failure-simulation.enabled:false}")
    private boolean failureSimulationEnabled;

    @Value("${payment.failure-simulation.threshold-amount:10000.00}")
    private BigDecimal failureThreshold;

    public PaymentServiceImpl(PaymentRepository paymentRepository, PaymentEventPublisher paymentEventPublisher) {
        this.paymentRepository = paymentRepository;
        this.paymentEventPublisher = paymentEventPublisher;
    }

    @Override
    @Transactional
    public PaymentResponse processOrderPayment(OrderPlacedEvent event) {
        if (event == null || event.orderNumber() == null) {
            log.warn("Received empty OrderPlacedEvent, skipping");
            return null;
        }

        String orderNumber = event.orderNumber();
        log.info("Processing payment for order {}", orderNumber);

        // 1. Enforce idempotency: check if orderNumber already exists
        Optional<Payment> existing = paymentRepository.findByOrderNumber(orderNumber);
        if (existing.isPresent()) {
            log.warn("Idempotency guard: payment for order '{}' already exists (reference: {}). Skipping reprocessing.",
                    orderNumber, existing.get().getPaymentReference());
            return mapToResponse(existing.get());
        }

        // 2. Simulate payment gateway execution
        boolean shouldFail = (failureSimulationEnabled && event.totalAmount() != null && event.totalAmount().compareTo(failureThreshold) > 0)
                || (event.userEmail() != null && event.userEmail().toLowerCase().contains("fail"))
                || (orderNumber.toUpperCase().contains("FAIL"));

        PaymentStatus status = shouldFail ? PaymentStatus.FAILED : PaymentStatus.SUCCESS;
        String failureReason = shouldFail ? "Payment declined by simulated gateway (card declined or limit exceeded)" : null;
        String paymentReference = "PAY-" + UUID.randomUUID().toString().replace("-", "").substring(0, 10).toUpperCase();

        Payment payment = new Payment(
                null,
                paymentReference,
                orderNumber,
                event.userId(),
                event.totalAmount(),
                status,
                failureReason,
                LocalDateTime.now()
        );

        Payment saved = paymentRepository.save(payment);
        log.info("Saved payment record {} for order {}: status={}", paymentReference, orderNumber, status);

        // 3. Publish PaymentProcessedEvent to payment-status-topic
        PaymentProcessedEvent processedEvent = new PaymentProcessedEvent(
                orderNumber,
                paymentReference,
                status.name(),
                failureReason,
                LocalDateTime.now()
        );

        paymentEventPublisher.publishPaymentProcessed(processedEvent);

        return mapToResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public PaymentResponse getPaymentByOrderNumber(String orderNumber) {
        Payment payment = paymentRepository.findByOrderNumber(orderNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Payment record not found for order number: " + orderNumber));
        return mapToResponse(payment);
    }

    private PaymentResponse mapToResponse(Payment payment) {
        return new PaymentResponse(
                payment.getPaymentReference(),
                payment.getOrderNumber(),
                payment.getUserId(),
                payment.getAmount(),
                payment.getStatus().name(),
                payment.getFailureReason(),
                payment.getProcessedAt()
        );
    }
}
