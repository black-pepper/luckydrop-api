package com.luckydrop.api.common.exception;

public class DrawEventException extends RuntimeException {

    private final ErrorCode errorCode;
    private final Long retryAfterSeconds;

    public DrawEventException(ErrorCode errorCode) {
        this(errorCode, null);
    }

    public DrawEventException(ErrorCode errorCode, long retryAfterSeconds) {
        this(errorCode, Long.valueOf(retryAfterSeconds));
    }

    public DrawEventException(ErrorCode errorCode, Long retryAfterSeconds) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
        this.retryAfterSeconds = retryAfterSeconds;
    }

    public ErrorCode getErrorCode() {
        return errorCode;
    }

    public Long getRetryAfterSeconds() {
        return retryAfterSeconds;
    }
}
