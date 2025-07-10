package com.vitacheck.config;

import com.vitacheck.dto.ErrorResponse;
import com.vitacheck.dto.ExceptionDetail;
import com.vitacheck.exception.CustomException;
import com.vitacheck.exception.ErrorCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(CustomException.class)
    public ResponseEntity<ErrorResponse> handleCustomException(CustomException ex) {
        ErrorCode errorCode = ex.getErrorCode();

        ExceptionDetail detail = new ExceptionDetail(errorCode.getCode(), errorCode.getMessage());
        ErrorResponse errorResponse = new ErrorResponse("error", "", detail);

        return ResponseEntity.status(errorCode.getStatus()).body(errorResponse);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleAllUnhandledException(Exception ex) {
        ErrorCode errorCode = ErrorCode.INTERNAL_ERROR;

        ExceptionDetail detail = new ExceptionDetail(errorCode.getCode(), errorCode.getMessage());
        ErrorResponse errorResponse = new ErrorResponse("error", "", detail);

        return ResponseEntity.status(errorCode.getStatus()).body(errorResponse);
    }
}
