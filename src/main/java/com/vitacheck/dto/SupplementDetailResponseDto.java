package com.vitacheck.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

// 영양제 상세 정보 (영양제 이미지, 영양제명, 브랜드명, 브랜드이미지, 쿠팡링크 등) 반환 DTO
@Getter
@AllArgsConstructor
@Builder
public class SupplementDetailResponseDto {

    private Long supplementId;
    private String brandName;
    private String brandImageUrl;
    private String supplementName;
    private String supplementImageUrl;
    private boolean liked;
    private String coupangLink;
    private String intakeTime;
    private List<IngredientDto> ingredients;

    @Getter
    @AllArgsConstructor
    @Builder
    public static class IngredientDto {
        private String name;
        private String amount;
    }
}
