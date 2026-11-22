package com.flopkart.payment;

import com.flopkart.common.event.OrderItemDto;
import com.flopkart.common.event.OrderPlacedEvent;
import com.flopkart.common.event.PaymentProcessedEvent;
import com.flopkart.payment.dto.PaymentResponse;
import com.flopkart.payment.entity.Payment;
import com.flopkart.payment.entity.PaymentStatus;
import com.flopkart.payment.event.PaymentEventPublisher;
import com.flopkart.payment.repository.PaymentRepository;
import com.flopkart.payment.service.PaymentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestPropertySource(properties = {
        "eureka.client.register-with-eureka=false",
        "eureka.client.fetch-registry=false",
        "spring.kafka.listener.auto-startup=false"
})
class PaymentServiceApplicationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private PaymentService paymentService;

    @MockBean
    private PaymentEventPublisher paymentEventPublisher;

    @BeforeEach
    void setUp() {
        paymentRepository.deleteAll();
        reset(paymentEventPublisher);
    }

    @Test
    @DisplayName("Application context loads successfully")
    void contextLoads() {
    }

    @Test
    @DisplayName("GET /api/v1/payments/status returns 200 OK and expected JSON status")
    void shouldReturnPaymentStatus() throws Exception {
        mockMvc.perform(get("/api/v1/payments/status")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Payment Service is operational"))
                .andExpect(jsonPath("$.data.service").value("payment-service"))
                .andExpect(jsonPath("$.data.status").value("UP"))
                .andExpect(jsonPath("$.data.port").value(8085));
    }

    @Test
    @DisplayName("processOrderPayment creates payment record and emits PaymentProcessedEvent with SUCCESS")
    void shouldProcessSuccessfulPayment() {
        OrderPlacedEvent event = new OrderPlacedEvent(
                "ORD-PAY-100",
                "10",
                "buyer@example.com",
                new BigDecimal("250.00"),
                List.of(new OrderItemDto(1L, "Item 1", 1, new BigDecimal("250.00"))),
                LocalDateTime.now()
        );

        PaymentResponse response = paymentService.processOrderPayment(event);

        assertNotNull(response);
        assertEquals("ORD-PAY-100", response.orderNumber());
        assertEquals("SUCCESS", response.status());
        assertNull(response.failureReason());

        // Check repository
        Payment saved = paymentRepository.findByOrderNumber("ORD-PAY-100").orElseThrow();
        assertEquals(PaymentStatus.SUCCESS, saved.getStatus());
        assertEquals("10", saved.getUserId());

        // Check event published
        ArgumentCaptor<PaymentProcessedEvent> captor = ArgumentCaptor.forClass(PaymentProcessedEvent.class);
        verify(paymentEventPublisher, times(1)).publishPaymentProcessed(captor.capture());
        PaymentProcessedEvent published = captor.getValue();
        assertEquals("ORD-PAY-100", published.orderNumber());
        assertEquals("SUCCESS", published.status());
    }

    @Test
    @DisplayName("Idempotency check: replaying exact same OrderPlacedEvent does not create duplicate payment")
    void shouldEnforceIdempotencyOnDuplicateMessages() {
        OrderPlacedEvent event = new OrderPlacedEvent(
                "ORD-IDEMPOTENT-1",
                "10",
                "buyer@example.com",
                new BigDecimal("150.00"),
                List.of(new OrderItemDto(1L, "Item 1", 1, new BigDecimal("150.00"))),
                LocalDateTime.now()
        );

        // First message
        PaymentResponse response1 = paymentService.processOrderPayment(event);
        assertEquals("SUCCESS", response1.status());
        assertEquals(1, paymentRepository.count());

        // Replay same message
        PaymentResponse response2 = paymentService.processOrderPayment(event);
        assertEquals(response1.paymentReference(), response2.paymentReference());
        assertEquals(1, paymentRepository.count(), "Repository count must remain 1 (no duplicate record created)");

        // Verify publisher was only called once for the first processing
        verify(paymentEventPublisher, times(1)).publishPaymentProcessed(any());
    }

    @Test
    @DisplayName("Simulated failure triggers FAILED status and publishes failed event")
    void shouldHandleSimulatedPaymentFailure() {
        // Amount exceeds 1000.00 threshold or email contains 'fail'
        OrderPlacedEvent event = new OrderPlacedEvent(
                "ORD-PAY-FAIL-999",
                "10",
                "failbuyer@example.com",
                new BigDecimal("5000.00"),
                List.of(new OrderItemDto(1L, "Luxury Item", 1, new BigDecimal("5000.00"))),
                LocalDateTime.now()
        );

        PaymentResponse response = paymentService.processOrderPayment(event);

        assertNotNull(response);
        assertEquals("ORD-PAY-FAIL-999", response.orderNumber());
        assertEquals("FAILED", response.status());
        assertNotNull(response.failureReason());

        Payment saved = paymentRepository.findByOrderNumber("ORD-PAY-FAIL-999").orElseThrow();
        assertEquals(PaymentStatus.FAILED, saved.getStatus());

        ArgumentCaptor<PaymentProcessedEvent> captor = ArgumentCaptor.forClass(PaymentProcessedEvent.class);
        verify(paymentEventPublisher, times(1)).publishPaymentProcessed(captor.capture());
        PaymentProcessedEvent published = captor.getValue();
        assertEquals("FAILED", published.status());
        assertNotNull(published.failureReason());
    }

    @Test
    @DisplayName("GET /api/v1/payments/order/{orderNumber} returns payment status")
    void shouldReturnPaymentByOrderNumber() throws Exception {
        Payment payment = new Payment(
                null,
                "PAY-TEST-REF",
                "ORD-LOOKUP-123",
                "10",
                new BigDecimal("99.99"),
                PaymentStatus.SUCCESS,
                null,
                LocalDateTime.now()
        );
        paymentRepository.save(payment);

        mockMvc.perform(get("/api/v1/payments/order/ORD-LOOKUP-123")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.orderNumber").value("ORD-LOOKUP-123"))
                .andExpect(jsonPath("$.data.paymentReference").value("PAY-TEST-REF"))
                .andExpect(jsonPath("$.data.status").value("SUCCESS"));
    }

    @Test
    @DisplayName("GET /api/v1/payments/order/{orderNumber} returns 404 for nonexistent payment")
    void shouldReturn404ForNonexistentPayment() throws Exception {
        mockMvc.perform(get("/api/v1/payments/order/ORD-NONEXISTENT")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false));
    }
}
