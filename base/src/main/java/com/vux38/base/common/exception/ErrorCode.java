package com.vux38.base.common.exception;

public enum ErrorCode {

    USER_NOT_FOUND("USER_001"),
    INVALID_PASSWORD("AUTH_001"),
    UNAUTHORIZED("AUTH_002"),
    VALIDATION_ERROR("SYS_001");

    private final String code;

    ErrorCode(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }
}