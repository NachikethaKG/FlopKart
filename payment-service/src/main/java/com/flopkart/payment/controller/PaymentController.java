package com.flopkart.payment.controller;

import com.flopkart.common.dto.ApiResponse;
import com.flopkart.payment.dto.PaymentResponse;
import com.flopkart.payment.service.PaymentService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/payments")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @GetMapping("/order/{orderNumber}")
    public ResponseEntity<ApiResponse<PaymentResponse>> getPaymentByOrderNumber(@PathVariable("orderNumber") String orderNumber) {
        PaymentResponse response = paymentService.getPaymentByOrderNumber(orderNumber);
        return ResponseEntity.ok(ApiResponse.success("Payment retrieved successfully", response));
    }
}
