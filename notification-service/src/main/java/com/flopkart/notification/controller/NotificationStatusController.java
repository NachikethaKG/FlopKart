package com.flopkart.notification.controller;

import com.flopkart.common.dto.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/notifications")
public class NotificationStatusController {

    @GetMapping("/status")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getStatus() {
        return ResponseEntity.ok(ApiResponse.success("Notification Service is operational", Map.of(
                "service", "notification-service",
                "status", "UP",
                "port", 8086
        )));
    }
}
