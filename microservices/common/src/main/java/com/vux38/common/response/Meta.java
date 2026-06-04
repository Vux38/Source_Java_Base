package com.vux38.common.response;

public record Meta(
        int status,
        String message,
        String traceId,
        long timestamp
) {}
