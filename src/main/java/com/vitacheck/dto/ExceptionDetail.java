package com.vitacheck.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ExceptionDetail {
    private String errorCode;
    private String errorMessage;
}
