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
    DUPLICATED_EMAIL("U0003", "이미 사용 중인 이메일입니다.", HttpStatus.BAD_REQUEST),
    PASSWORD_NOT_MATCH("U0004", "비밀번호가 일치하지 않습니다.", HttpStatus.BAD_REQUEST),

    // 영양제
    SUPPLEMENT_NOT_FOUND("S0001", "영양제를 찾을 수 없습니다.", HttpStatus.BAD_REQUEST),

    // 외부 API 에러
    EXTERNAL_API_ERROR("A0001", "외부 API 호출에 실패했습니다.", HttpStatus.SERVICE_UNAVAILABLE),

    // AWS
    S3_UPLOAD_ERROR("S30001", "S3 업로드에 실패했습니다.", HttpStatus.CONFLICT),

    SEARCH_KEYWORD_EMPTY("S0002", "검색 파라미터가 하나 이상 필요합니다.", HttpStatus.BAD_REQUEST),
    SUPPLEMENT_LIST_EMPTY("S0003", "분석할 영양제 목록이 비어있습니다.", HttpStatus.BAD_REQUEST),


    // 복용 루틴
    DUPLICATED_ROUTINE("R0001", "이미 등록된 복용 루틴입니다.", HttpStatus.CONFLICT),
    ROUTINE_NOT_FOUND("R0002", "복용 루틴을 찾을 수 없습니다.", HttpStatus.NOT_FOUND),

    // FCM 관련
    FCM_CONFIG_NOT_FOUND("F0001", "FCM 설정(키 파일 경로 또는 JSON 값)을 찾을 수 없습니다.", HttpStatus.INTERNAL_SERVER_ERROR),
    FCM_FILE_NOT_FOUND("F0002", "지정된 경로에서 FCM 키 파일을 찾을 수 없습니다.", HttpStatus.INTERNAL_SERVER_ERROR);


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
