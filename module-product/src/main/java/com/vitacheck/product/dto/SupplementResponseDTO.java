package com.vitacheck.product.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

public class SupplementResponseDTO {
    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class SupplementInfo{
        private Long id;
        private String name;
        private String coupangUrl;
        private String imageUrl;
    }
}
