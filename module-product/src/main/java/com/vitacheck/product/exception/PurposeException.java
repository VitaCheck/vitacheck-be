package com.vitacheck.product.exception;

import com.vitacheck.common.code.BaseErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum PurposeException implements BaseErrorCode {
    PURPOSE_NOT_FOUND("PURPOSE404", "존재하지 않는 목적입니다.", HttpStatus.NOT_FOUND);

    private final String code;
    private final String message;
    private final HttpStatus httpStatus;
}