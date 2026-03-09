package com.luckydrop.api.common.exception;

public class DrawEventException extends RuntimeException {

    private final ErrorCode errorCode;

    public DrawEventException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }

    public ErrorCode getErrorCode() {
        return errorCode;
    }
}
