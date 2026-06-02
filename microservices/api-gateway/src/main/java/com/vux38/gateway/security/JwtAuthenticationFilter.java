package com.vux38.gateway.security;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.List;

import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.server.ServerWebExchange;

import io.jsonwebtoken.Claims;
import reactor.core.publisher.Mono;

@Component
public class JwtAuthenticationFilter implements GlobalFilter, Ordered {

    private static final String BEARER_PREFIX = "Bearer ";

    private final JwtTokenValidator tokenValidator;
    private final AntPathMatcher pathMatcher = new AntPathMatcher();
    private final List<String> publicPaths = List.of(
            "/api/auth/register",
            "/api/auth/login",
            "/api/*/health"
    );
    private final List<String> protectedPaths = List.of(
            "/api/users/**",
            "/api/products/**",
            "/api/product/**"
    );

    public JwtAuthenticationFilter(JwtTokenValidator tokenValidator) {
        this.tokenValidator = tokenValidator;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, org.springframework.cloud.gateway.filter.GatewayFilterChain chain) {
        String path = exchange.getRequest().getPath().pathWithinApplication().value();

        if (matches(publicPaths, path) || !matches(protectedPaths, path)) {
            return chain.filter(exchange);
        }

        String authorization = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (authorization == null || !authorization.startsWith(BEARER_PREFIX)) {
            return unauthorized(exchange, "Missing bearer token");
        }

        String token = authorization.substring(BEARER_PREFIX.length());
        try {
            Claims claims = tokenValidator.validate(token);
            ServerWebExchange authenticatedExchange = exchange.mutate()
                    .request(builder -> builder.header("X-Authenticated-User", claims.getSubject()))
                    .build();
            return chain.filter(authenticatedExchange);
        } catch (Exception ex) {
            return unauthorized(exchange, "Invalid or expired token");
        }
    }

    @Override
    public int getOrder() {
        return -100;
    }

    private boolean matches(List<String> patterns, String path) {
        return patterns.stream().anyMatch(pattern -> pathMatcher.match(pattern, path));
    }

    private Mono<Void> unauthorized(ServerWebExchange exchange, String message) {
        exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
        exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);

        String body = """
                {"success":false,"message":"%s","timestamp":"%s"}
                """.formatted(message, Instant.now());

        byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
        return exchange.getResponse().writeWith(Mono.just(exchange.getResponse().bufferFactory().wrap(bytes)));
    }
}
