package com.vitacheck.dto;

import lombok.Getter;
import java.util.List;

// 사용자로부터 목적을 ENUM으로 받아온다
@Getter
public class SupplementPurposeRequest {
    private List<String> purposeNames;
}

