package com.vitacheck.global.apiPayload.code;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode implements BaseErrorCode {
    // 원하시는 에러 종류 계속 추가해주세용
    // 기본 에러
    INVALID_REQUEST("CE0001", "요청이 올바르지 않습니다.", HttpStatus.BAD_REQUEST),
    INTERNAL_ERROR("CE0500", "서버 내부 오류입니다.", HttpStatus.INTERNAL_SERVER_ERROR),

    // 유저
    UNAUTHORIZED("U0001", "로그인이 필요합니다.", HttpStatus.UNAUTHORIZED),
    USER_NOT_FOUND("U0002", "사용자를 찾을 수 없습니다.", HttpStatus.NOT_FOUND),

    // 영양제
    SUPPLEMENT_NOT_FOUND("S0001", "영양제를 찾을 수 없습니다.", HttpStatus.BAD_REQUEST),

    // 외부 API 에러
    EXTERNAL_API_ERROR("A0001", "외부 API 호출에 실패했습니다.", HttpStatus.SERVICE_UNAVAILABLE);


    private final String code;
    private final String message;
    private final HttpStatus status;

    ErrorCode(String code, String message, HttpStatus status) {
        this.code = code;
        this.message = message;
        this.status = status;
    }

    @Override
    public String getCode() {
        return code;
    }
    @Override
    public String getMessage() {
        return message;
    }

    @Override
    public HttpStatus getHttpStatus() {
        return status;
    }
}
