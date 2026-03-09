package com.luckydrop.api.common.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode {

    CODE_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 코드입니다."),
    CODE_INACTIVE(HttpStatus.BAD_REQUEST, "비활성화된 코드입니다."),
    CODE_EXPIRED(HttpStatus.BAD_REQUEST, "만료된 코드입니다."),
    CODE_NO_REMAINING(HttpStatus.BAD_REQUEST, "남은 횟수가 없습니다."),
    PARTICIPANT_INACTIVE(HttpStatus.BAD_REQUEST, "비활성화된 사용자입니다."),
    NO_AVAILABLE_REWARD(HttpStatus.INTERNAL_SERVER_ERROR, "추첨 가능한 보상이 없습니다."),
    INTERNAL_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "서버 오류가 발생했습니다.");

    private final HttpStatus status;
    private final String message;

    ErrorCode(HttpStatus status, String message) {
        this.status = status;
        this.message = message;
    }
}
