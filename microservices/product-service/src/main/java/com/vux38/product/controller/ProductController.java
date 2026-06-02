package com.vux38.product.controller;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    private static final List<ProductData> PRODUCTS = List.of(
            new ProductData(1L, "Laptop Pro 14", "Electronics", BigDecimal.valueOf(1299.99), 12),
            new ProductData(2L, "Mechanical Keyboard", "Accessories", BigDecimal.valueOf(89.50), 40));

    @GetMapping("/health")
    public ApiResponse<HealthData> health() {
        return ApiResponse.ok(
                "Product service is running",
                new HealthData("product-service", "UP"));
    }

    @GetMapping
    public ApiResponse<List<ProductData>> findAll() {
        return ApiResponse.ok("Products loaded successfully", PRODUCTS);
    }

    @GetMapping("/{id}")
    public ApiResponse<ProductData> findById(@PathVariable("id") Long id) {
        ProductData product = PRODUCTS.stream()
                .filter(item -> item.id().equals(id))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Product not found: " + id));

        return ApiResponse.ok("Product loaded successfully", product);
    }

    public record ApiResponse<T>(boolean success, String message, T data, String timestamp) {
        public static <T> ApiResponse<T> ok(String message, T data) {
            return new ApiResponse<>(true, message, data, Instant.now().toString());
        }
    }

    public record HealthData(String service, String status) {
    }

    public record ProductData(Long id, String name, String category, BigDecimal price, int stockQuantity) {
    }
}
