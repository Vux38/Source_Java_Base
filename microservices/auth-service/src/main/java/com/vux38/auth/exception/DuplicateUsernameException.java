package com.vux38.auth.exception;

/**
 * Exception thrown when attempting to register with an existing username.
 *
 * @author VUX38
 * @version 1.0
 * @since 2026
 */
public class DuplicateUsernameException extends RuntimeException {

    public DuplicateUsernameException(String message) {
        super(message);
    }

    public DuplicateUsernameException(String message, Throwable cause) {
        super(message, cause);
    }
}