package com.vitacheck.dto;

import com.vitacheck.domain.Terms;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

public class TermsDto {

    @Getter
    @Builder
    @Schema(description = "약관 조회 응답 DTO")
    public static class TermResponse {
        private Long id;
        private String title;
        private String content;
        private String version;
        private boolean isRequired;
        private LocalDate effectiveDate;

        public static TermResponse from(Terms term) {
            return TermResponse.builder()
                    .id(term.getId())
                    .title(term.getTitle())
                    .content(term.getContent())
                    .version(term.getVersion())
                    .isRequired(term.isRequired())
                    .effectiveDate(term.getEffectiveDate())
                    .build();
        }
    }

    @Getter
    @NoArgsConstructor
    @Schema(description = "약관 동의 요청 DTO")
    public static class AgreementRequest {
        @NotEmpty(message = "하나 이상의 약관에 동의해야 합니다.")
        @Schema(description = "사용자가 동의한 약관의 ID 목록", example = "[1, 2]")
        private List<Long> agreedTermIds;
    }

    @Getter
    @NoArgsConstructor
    @Schema(description = "약관 동의 철회 요청 DTO")
    public static class AgreementWithdrawalRequest {
        @NotEmpty(message = "하나 이상의 약관을 선택해야 합니다.")
        @Schema(description = "사용자가 동의를 철회할 약관의 ID 목록", example = "[3]")
        private List<Long> withdrawnTermIds;
    }
}