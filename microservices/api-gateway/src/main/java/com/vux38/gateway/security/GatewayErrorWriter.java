package com.vux38.gateway.security;

import java.time.Instant;

import com.vux38.common.constant.Headers;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import reactor.core.publisher.Mono;

/**
 * Writes gateway error responses in one consistent JSON format.
 */
@Component
public class GatewayErrorWriter {

    private final ObjectMapper objectMapper;

    /**
     * Creates a writer backed by Spring Boot's configured Jackson mapper.
     *
     * @param objectMapper mapper used to serialize response bodies
     */
    public GatewayErrorWriter(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    /**
     * Writes an error response to the current exchange.
     *
     * @param exchange current web exchange
     * @param status HTTP status to return
     * @param message message to include in the response body
     * @return completion signal for the response write
     */
    public Mono<Void> write(ServerWebExchange exchange, HttpStatus status, String message) {
        String path = exchange.getRequest().getPath().pathWithinApplication().value();
        String traceId = exchange.getRequest().getHeaders().getFirst(Headers.TRACE_ID);

        GatewayErrorResponse response = new GatewayErrorResponse(
                status.value(),
                false,
                message,
                path,
                traceId,
                Instant.now().toString()
        );

        exchange.getResponse().setStatusCode(status);
        exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);

        byte[] bytes = serialize(response);
        return exchange.getResponse().writeWith(Mono.just(exchange.getResponse().bufferFactory().wrap(bytes)));
    }

    private byte[] serialize(GatewayErrorResponse response) {
        try {
            return objectMapper.writeValueAsBytes(response);
        } catch (JsonProcessingException ex) {
            throw new IllegalStateException("Could not serialize gateway error response", ex);
        }
    }
}
