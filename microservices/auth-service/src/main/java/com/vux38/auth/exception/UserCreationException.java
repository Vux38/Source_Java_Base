package com.vux38.auth.exception;

/**
 * Exception thrown when user profile creation fails.
 *
 * @author VUX38
 * @version 1.0
 */
public class UserCreationException extends RuntimeException {

    public UserCreationException(String message, Throwable cause) {
        super(message, cause);
    }
}