package com.auth.project.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.FORBIDDEN)
public class TokenRefreshException extends AppException {
    public TokenRefreshException(String token, String message) {
        super("Failed for token [" + token + "]: " + message);
    }
}
