package com.luckydrop.api.common.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode {

    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "인증 정보가 없습니다."),
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 인증 정보에 매핑된 사용자를 찾을 수 없습니다."),
    CONTENT_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 콘텐츠입니다."),
    CONTENT_ALREADY_DELETED(HttpStatus.BAD_REQUEST, "이미 삭제된 콘텐츠입니다."),
    CONTENT_TYPE_INVALID(HttpStatus.BAD_REQUEST, "콘텐츠 타입이 올바르지 않습니다."),
    FORBIDDEN_CONTENT_ACCESS(HttpStatus.FORBIDDEN, "본인 소유의 콘텐츠에만 접근할 수 있습니다."),
    CODE_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 코드입니다."),
    CODE_INACTIVE(HttpStatus.BAD_REQUEST, "비활성화된 코드입니다."),
    CODE_EXPIRED(HttpStatus.BAD_REQUEST, "만료된 코드입니다."),
    CODE_NO_REMAINING(HttpStatus.BAD_REQUEST, "남은 횟수가 없습니다."),
    PARTICIPANT_INACTIVE(HttpStatus.BAD_REQUEST, "비활성화된 사용자입니다."),
    DRAW_RESULT_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 추첨 결과입니다."),
    DRAW_CODE_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 초대 코드입니다."),
    DRAW_CODE_DUPLICATE(HttpStatus.CONFLICT, "이미 존재하는 초대 코드입니다."),
    REWARD_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 보상입니다."),
    REWARD_OUT_OF_STOCK(HttpStatus.BAD_REQUEST, "보상 재고가 부족합니다."),
    INVITATION_CODE_USAGE_EXCEEDED(HttpStatus.BAD_REQUEST, "초대 코드의 사용 횟수를 초과했습니다."),
    NO_AVAILABLE_REWARD(HttpStatus.INTERNAL_SERVER_ERROR, "추첨 가능한 보상이 없습니다."),
    INTERNAL_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "서버 오류가 발생했습니다.");

    private final HttpStatus status;
    private final String message;

    ErrorCode(HttpStatus status, String message) {
        this.status = status;
        this.message = message;
    }
}
