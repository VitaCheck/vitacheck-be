package com.vitacheck.ai.dto;

import lombok.Getter;
import java.util.List;

@Getter
public class AiRecommendationRequestDto {
    // API에 요청을 보낼 때 사용하는 데이터 형식
    // "어떤 목적들을 원하시나요?"
    private List<String> purposes; // 예: ["눈 건강", "피로 개선"]
}