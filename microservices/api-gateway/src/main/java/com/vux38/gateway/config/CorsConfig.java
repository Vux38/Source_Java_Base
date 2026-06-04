package com.vux38.gateway.config;

import java.util.List;

import com.vux38.common.constant.Headers;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsWebFilter;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;

/**
 * Configures cross-origin requests for browser clients that call the gateway.
 */
@Configuration
public class CorsConfig {

    /**
     * Allows common local frontend origins to call gateway APIs.
     *
     * @return WebFlux CORS filter applied before route handling
     */
    @Bean
    CorsWebFilter corsWebFilter() {
        CorsConfiguration cors = new CorsConfiguration();
        cors.setAllowedOrigins(List.of(
                "http://localhost:3000",
                "http://localhost:4200",
                "http://localhost:5173",
                "http://127.0.0.1:3000",
                "http://127.0.0.1:4200",
                "http://127.0.0.1:5173"
        ));
        cors.setAllowedMethods(List.of(
                HttpMethod.GET.name(),
                HttpMethod.POST.name(),
                HttpMethod.PUT.name(),
                HttpMethod.PATCH.name(),
                HttpMethod.DELETE.name(),
                HttpMethod.OPTIONS.name()
        ));
        cors.setAllowedHeaders(List.of("*"));
        cors.setExposedHeaders(List.of(
                Headers.TRACE_ID,
                Headers.AUTHENTICATED_USER,
                Headers.AUTHENTICATED_ROLES,
                HttpHeaders.AUTHORIZATION
        ));
        cors.setAllowCredentials(true);
        cors.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", cors);
        return new CorsWebFilter(source);
    }
}
