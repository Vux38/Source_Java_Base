package com.vux38.gateway.filter;

import java.util.Optional;
import java.util.UUID;

import com.vux38.common.constant.Headers;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;

import reactor.core.publisher.Mono;

@Component
public class TraceIdFilter implements GlobalFilter, Ordered {

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {

        String incomingTraceId = exchange.getRequest()
                .getHeaders()
                .getFirst(Headers.TRACE_ID);

        String traceId = Optional.ofNullable(
                exchange.getRequest().getHeaders().getFirst(Headers.TRACE_ID)
        ).orElse(UUID.randomUUID().toString());

        return chain.filter(
                exchange.mutate()
                        .request(builder -> builder.header(Headers.TRACE_ID, traceId))
                        .build()
        );
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
            String traceId = exchange.getRequest().getHeaders().getFirst(Headers.TRACE_ID);
            if (traceId == null || traceId.isBlank()) {
                    return UUID.randomUUID().toString();
                }
            return traceId;
        }
}