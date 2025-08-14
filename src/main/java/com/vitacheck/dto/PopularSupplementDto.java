package com.vitacheck.dto;

import com.vitacheck.domain.Supplement;
import lombok.Builder;
import lombok.Getter;

@Getter
public class PopularSupplementDto {

    private Long supplementId;
    private String supplementName;
    private String brandName;
    private String imageUrl;
    private long searchCount;

    @Builder
    private PopularSupplementDto(Long supplementId, String supplementName, String brandName, String imageUrl, long searchCount) {
        this.supplementId = supplementId;
        this.supplementName = supplementName;
        this.brandName = brandName;
        this.imageUrl = imageUrl;
        this.searchCount = searchCount;
    }

    // Tuple 결과를 DTO로 변환하는 정적 메소드
    public static PopularSupplementDto from(Supplement supplement, long searchCount) {
        return PopularSupplementDto.builder()
                .supplementId(supplement.getId())
                .supplementName(supplement.getName())
                .brandName(supplement.getBrand().getName())
                .imageUrl(supplement.getImageUrl())
                .searchCount(searchCount)
                .build();
    }
}
