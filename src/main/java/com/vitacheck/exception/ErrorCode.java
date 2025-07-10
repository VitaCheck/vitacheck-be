package com.vitacheck.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode {
    // 원하시는 에러 종류 계속 추가해주세용
    UNAUTHORIZED("CE0001", "로그인이 필요합니다.", HttpStatus.UNAUTHORIZED),
    USER_NOT_FOUND("CE0002", "사용자를 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
    INVALID_REQUEST("CE0003", "요청이 올바르지 않습니다.", HttpStatus.BAD_REQUEST),
    INTERNAL_ERROR("CE0500", "서버 내부 오류입니다.", HttpStatus.INTERNAL_SERVER_ERROR);

    private final String code;
    private final String message;
    private final HttpStatus status;

    ErrorCode(String code, String message, HttpStatus status) {
        this.code = code;
        this.message = message;
        this.status = status;
    }
}
