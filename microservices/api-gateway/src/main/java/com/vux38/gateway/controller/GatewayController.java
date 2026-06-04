package com.vux38.gateway.controller;

import java.time.Instant;
import java.util.List;

import com.vux38.common.response.ApiResponse;
import com.vux38.common.response.HealthData;
import com.vux38.common.response.RouteData;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/gateway")
public class GatewayController {

    @GetMapping("/health")
    public ApiResponse<HealthData> health() {
        return ApiResponse.ok(
                "AAGateway service is running",
                new HealthData("api-gateway", "UP"));
    }

    @GetMapping("/routes")
    public ApiResponse<List<RouteData>> routes() {
//        System.out.println("SSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSS");
        List<RouteData> routes = List.of(
                new RouteData("auth-service", "http://localhost:8081", "/api/auth/**"),
                new RouteData("user-service", "http://localhost:8082", "/api/users/**"),
                new RouteData("product-service", "http://localhost:8083", "/api/products/**"));

        return ApiResponse.ok(
                "Gateway routes loaded successfully3",
                routes);
    }

}