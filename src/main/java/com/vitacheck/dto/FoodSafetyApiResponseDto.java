package com.vitacheck.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.ToString;

import java.util.List;

@Getter
@ToString
public class FoodSafetyApiResponseDto {

    @JsonProperty("C003")
    private C003 c003;

    @Getter
    @ToString
    public static class C003 {
        @JsonProperty("total_count")
        private String totalCount;

        @JsonProperty("row")
        private List<SupplementInfo> supplements;

        @JsonProperty("RESULT")
        private Result result;
    }

    @Getter
    @ToString
    public static class SupplementInfo {

        @JsonProperty("PRIMARY_FNCLTY")
        private String primaryFunction; // 주된 기능성

        @JsonProperty("HF_FNCLTY_MTRAL_RCOGN_NO")
        private String functionalMaterialRecognitionNo; // 원료인정번호

        @JsonProperty("DAY_INTK_HIGHLIMIT")
        private String dayIntakeHighLimit; // 1일 섭취량 상한선

        @JsonProperty("DAY_INTK_LOWLIMIT")
        private String dayIntakeLowLimit; // 1일 섭취량 하한선

        @JsonProperty("WT_UNIT")
        private String weightUnit; // 중량 단위

        @JsonProperty("RAWMTRL_NM")
        private String rawMaterialName; // 원재료 명

        @JsonProperty("IFTKN_ATNT_MATR_CN")
        private String intakeCaution; // 섭취시 주의 사항 내용
    }

    @Getter
    @ToString
    public static class Result {
        @JsonProperty("MSG")
        private String message;

        @JsonProperty("CODE")
        private String code;
    }
}
