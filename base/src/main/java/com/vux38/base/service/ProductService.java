package com.vux38.base.service;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Service
public class ProductService {

    @Cacheable(value = "products", key = "#id")
    public String getProduct(Long id) {
        simulateSlowCall();
        return "Product " + id;
    }

    private void simulateSlowCall() {
        try { Thread.sleep(2000); } catch (InterruptedException ignored) {}
    }
}