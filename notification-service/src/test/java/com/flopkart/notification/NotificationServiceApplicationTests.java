package com.flopkart.notification;

import com.flopkart.common.event.OrderItemDto;
import com.flopkart.common.event.OrderPlacedEvent;
import com.flopkart.common.event.PaymentProcessedEvent;
import com.flopkart.notification.consumer.NotificationKafkaConsumer;
import com.flopkart.notification.service.NotificationDispatcher;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
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

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
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
class NotificationServiceApplicationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private NotificationKafkaConsumer notificationKafkaConsumer;

    @MockBean
    private NotificationDispatcher notificationDispatcher;

    @Test
    @DisplayName("Application context loads successfully")
    void contextLoads() {
    }

    @Test
    @DisplayName("GET /api/v1/notifications/status returns 200 OK and expected JSON status")
    void shouldReturnNotificationStatus() throws Exception {
        mockMvc.perform(get("/api/v1/notifications/status")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Notification Service is operational"))
                .andExpect(jsonPath("$.data.service").value("notification-service"))
                .andExpect(jsonPath("$.data.status").value("UP"))
                .andExpect(jsonPath("$.data.port").value(8086));
    }

    @Test
    @DisplayName("NotificationKafkaConsumer handles OrderPlacedEvent")
    void shouldHandleOrderPlacedEvent() {
        OrderPlacedEvent event = new OrderPlacedEvent(
                "ORD-NOTIFY-001",
                "10",
                "notify@example.com",
                new BigDecimal("199.99"),
                List.of(new OrderItemDto(1L, "Widget", 1, new BigDecimal("199.99"))),
                LocalDateTime.now()
        );

        notificationKafkaConsumer.consumeOrderPlacedEvent(event);

        verify(notificationDispatcher, times(1)).dispatchOrderPlacedNotification(event);
    }

    @Test
    @DisplayName("NotificationKafkaConsumer handles payment SUCCESS event")
    void shouldHandlePaymentSuccessEvent() {
        PaymentProcessedEvent event = new PaymentProcessedEvent(
                "ORD-NOTIFY-002",
                "PAY-5555",
                "SUCCESS",
                null,
                LocalDateTime.now()
        );

        notificationKafkaConsumer.consumePaymentProcessedEvent(event);

        verify(notificationDispatcher, times(1)).dispatchPaymentSuccessNotification(event);
    }

    @Test
    @DisplayName("NotificationKafkaConsumer handles payment FAILED event")
    void shouldHandlePaymentFailedEvent() {
        PaymentProcessedEvent event = new PaymentProcessedEvent(
                "ORD-NOTIFY-003",
                "PAY-6666",
                "FAILED",
                "Declined by bank",
                LocalDateTime.now()
        );

        notificationKafkaConsumer.consumePaymentProcessedEvent(event);

        verify(notificationDispatcher, times(1)).dispatchPaymentFailedNotification(event);
    }
}
