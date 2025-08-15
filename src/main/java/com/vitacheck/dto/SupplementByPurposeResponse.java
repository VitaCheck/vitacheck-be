package com.vitacheck.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
@Builder
public class SupplementByPurposeResponse {
    private Long id;
    private List<String> purposes; // 해당 성분이 속한 목적들
    private List<List<String>> supplements; // [영양제 이름, 이미지 URL]
}

