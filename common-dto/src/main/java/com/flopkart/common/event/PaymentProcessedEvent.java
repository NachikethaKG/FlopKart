package com.flopkart.common.event;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Immutable event emitted after a payment transaction has been processed (successful or failed).
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public record PaymentProcessedEvent(
        String orderNumber,
        String paymentId,
        String status,
        String failureReason,
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS")
        LocalDateTime timestamp
) implements Serializable {

    private static final long serialVersionUID = 1L;

    public PaymentProcessedEvent {
        if (timestamp == null) {
            timestamp = LocalDateTime.now();
        }
    }

    @JsonIgnore
    public boolean isSuccessful() {
        return "SUCCESS".equalsIgnoreCase(status);
    }
}
