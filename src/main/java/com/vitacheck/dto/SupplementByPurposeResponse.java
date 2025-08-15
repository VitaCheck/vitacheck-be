package com.vitacheck.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@AllArgsConstructor
@Builder
public class SupplementByPurposeResponse {

    private Long id; // (선택) 이건 ingredientId 등 명확한 의미로 쓰세요

    private List<String> purposes;

    private List<SupplementBrief> supplements;

    @Getter @Builder @AllArgsConstructor @NoArgsConstructor
    public static class SupplementBrief {
        private Long id;
        private String name;
        private String imageUrl;
    }
}

