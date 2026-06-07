package com.vux38.auth.exception;

/**
 * Exception thrown for token-related errors.
 * <p>
 * This exception is used for:
 * <ul>
 *   <li>Invalid or malformed tokens</li>
 *   <li>Expired tokens</li>
 *   <li>Tokens not found in database</li>
 *   <li>Token signature validation failures</li>
 * </ul>
 * </p>
 *
 * @author VUX38
 * @version 1.0
 * @since 2026
 */
public class TokenException extends RuntimeException {

    /**
     * Constructs a new TokenException with the specified message.
     *
     * @param message the detail message
     */
    public TokenException(String message) {
        super(message);
    }

    /**
     * Constructs a new TokenException with the specified message and cause.
     *
     * @param message the detail message
     * @param cause the cause of the exception
     */
    public TokenException(String message, Throwable cause) {
        super(message, cause);
    }
}