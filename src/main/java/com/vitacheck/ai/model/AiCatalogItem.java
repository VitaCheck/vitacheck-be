package com.vitacheck.ai.model;

import java.util.List;

public record AiCatalogItem(
        Long id,
        String name,
        List<String> purposes,   // 해당 영양제가 커버하는 목적 키워드들
        List<String> ingredients // 표시용(프롬프트 가이드)
) {}