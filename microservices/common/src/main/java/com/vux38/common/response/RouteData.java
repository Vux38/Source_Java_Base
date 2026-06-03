package com.vux38.common.response;

public record RouteData(
        String service,
        String target,
        String path
) {
}