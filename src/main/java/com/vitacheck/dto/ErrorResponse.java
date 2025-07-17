package com.vitacheck.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ErrorResponse {
    private String status; // 항상 error
    private String message;
    private ExceptionDetail exception;
}
