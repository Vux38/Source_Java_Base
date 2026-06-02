package com.vux38.gateway.controller;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/gateway")
public class GatewayController {

    @GetMapping("/health")
    public ApiResponse<HealthData> health() {
        return ApiResponse.ok(
                "Gateway service is running",
                new HealthData("api-gateway", "UP"));
    }

    @GetMapping("/routes")
    public ApiResponse<List<RouteData>> routes() {
        List<RouteData> routes = List.of(
                new RouteData("auth-service", "http://localhost:8081", "/api/auth/**"),
                new RouteData("user-service", "http://localhost:8082", "/api/users/**"),
                new RouteData("product-service", "http://localhost:8083", "/api/products/**"));

        return ApiResponse.ok(
                "Gateway routes loaded successfully",
                routes);
    }

    // =========================
    // RESPONSE WRAPPER
    // =========================

    public record ApiResponse<T>(
            Meta meta,
            T data
    ) {

        public static <T> ApiResponse<T> ok(String message, T data) {
            return new ApiResponse<>(
                    new Meta(
                            200,
                            message,
                            UUID.randomUUID().toString(),
                            Instant.now().toEpochMilli()
                    ),
                    data
            );
        }
    }

    public record Meta(
            int status,
            String message,
            String traceId,
            long timestamp
    ) {
    }

    // =========================
    // DATA OBJECTS
    // =========================

    public record HealthData(
            String service,
            String status
    ) {
    }

    public record RouteData(
            String service,
            String target,
            String path
    ) {
    }
}