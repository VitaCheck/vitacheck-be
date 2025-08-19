package com.vitacheck.dto;

import com.vitacheck.domain.purposes.AllPurpose;
import com.vitacheck.domain.user.Gender;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

public class PurposeResponseDTO {
    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class PurposeWithIngredientSupplement{
        private AllPurpose name; // 목적 이름
        // 해당 목적에 매핑된 성분들 + 각 성분별 영양제
        private List<IngredientResponseDTO.IngredientWithSupplement> ingredients;
    }


}
