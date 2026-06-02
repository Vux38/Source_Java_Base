package com.vux38.gateway.security;

/**
 * Defines HTTP header names used by the API gateway when forwarding authenticated requests.
 */
public final class GatewayHeaders {

    /**
     * Correlation identifier added to every request and response handled by the gateway.
     */
    public static final String TRACE_ID = "X-Trace-Id";

    /**
     * Username extracted from a verified JWT and forwarded to downstream services.
     */
    public static final String AUTHENTICATED_USER = "X-Authenticated-User";

    /**
     * Comma-separated roles extracted from a verified JWT and forwarded to downstream services.
     */
    public static final String AUTHENTICATED_ROLES = "X-Authenticated-Roles";

    private GatewayHeaders() {
    }
}
