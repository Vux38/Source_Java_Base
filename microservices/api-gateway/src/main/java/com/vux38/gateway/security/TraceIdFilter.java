package com.vux38.gateway.security;

import java.util.UUID;

import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;

import reactor.core.publisher.Mono;

/**
 * Ensures every request handled by the gateway has a trace id.
 */
@Component
public class TraceIdFilter implements GlobalFilter, Ordered {

    /**
     * Adds a trace id to the incoming request headers and outgoing response headers.
     *
     * @param exchange current web exchange
     * @param chain next filter in the gateway chain
     * @return completion signal for the filter chain
     */
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String traceId = resolveTraceId(exchange);

        ServerWebExchange tracedExchange = exchange.mutate()
                .request(builder -> builder.header(GatewayHeaders.TRACE_ID, traceId))
                .build();

        tracedExchange.getResponse().getHeaders().set(GatewayHeaders.TRACE_ID, traceId);
        return chain.filter(tracedExchange);
    }

    /**
     * Runs before authentication so gateway error responses also include a trace id.
     *
     * @return gateway filter order
     */
    @Override
    public int getOrder() {
        return -200;
    }

    private String resolveTraceId(ServerWebExchange exchange) {
        String traceId = exchange.getRequest().getHeaders().getFirst(GatewayHeaders.TRACE_ID);
        if (traceId == null || traceId.isBlank()) {
            return UUID.randomUUID().toString();
        }
        return traceId;
    }
}
