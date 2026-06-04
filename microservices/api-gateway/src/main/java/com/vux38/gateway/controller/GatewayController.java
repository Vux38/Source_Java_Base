package com.vux38.gateway.controller;

import java.util.List;

import com.vux38.common.response.ApiResponse;
import com.vux38.common.response.HealthData;
import com.vux38.common.response.RouteData;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Gateway management endpoints.
 */
@RestController
@RequestMapping("/api/gateway")
public class GatewayController {

    private static final String TRACE_ID_HEADER = "X-Trace-Id";

    @GetMapping("/health")
    public ApiResponse<HealthData> health(ServerHttpRequest request) {
        return ApiResponse.ok(
                "Gateway service is running",
                new HealthData("api-gateway", "UP"),
                traceId(request)
        );
    }

    @GetMapping("/routes")
    public ApiResponse<List<RouteData>> routes(ServerHttpRequest request) {
        System.out.println(traceId(request));
        List<RouteData> routes = List.of(
                new RouteData(
                        "auth-service",
                        "http://localhost:8081",
                        "/api/auth/**"),
                new RouteData(
                        "user-service",
                        "http://localhost:8082",
                        "/api/users/**"),
                new RouteData(
                        "product-service",
                        "http://localhost:8083",
                        "/api/products/**"));

        return ApiResponse.ok(
                "Gateway routes loaded successfully",
                routes,
                traceId(request)
        );
    }

    private String traceId(ServerHttpRequest request) {
        return request.getHeaders().getFirst(TRACE_ID_HEADER);
    }
}