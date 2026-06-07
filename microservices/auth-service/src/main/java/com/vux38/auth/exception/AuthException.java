package com.vux38.auth.exception;

/**
 * Base exception for authentication-related errors.
 *
 * @author VUX38
 * @version 1.0
 */
public class AuthException extends RuntimeException {

    public AuthException(String message) {
        super(message);
    }

    public AuthException(String message, Throwable cause) {
        super(message, cause);
    }
}