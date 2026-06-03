package com.vux38.common.response;

public record HealthData(
        String service,
        String status
) {
}