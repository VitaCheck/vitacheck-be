package com.vitacheck.Activity.exception;

import com.vitacheck.common.code.BaseErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ActivityException implements BaseErrorCode {
    CATEGORY_NOT_FOUND("ACTIVITY404", "존재하지 않는 카테고리입니다.", HttpStatus.NOT_FOUND);

    private final String code;
    private final String message;
    private final HttpStatus httpStatus;
}