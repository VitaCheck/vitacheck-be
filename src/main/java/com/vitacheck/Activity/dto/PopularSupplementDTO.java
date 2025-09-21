package com.vitacheck.Activity.dto;


import com.vitacheck.product.domain.Supplement.Supplement;
import lombok.Builder;
import lombok.Getter;

@Getter
public class PopularSupplementDTO {

    private Long supplementId;
    private String supplementName;
    private String brandName;
    private String imageUrl;
    private long searchCount;

    @Builder
    private PopularSupplementDTO(Long supplementId, String supplementName, String brandName, String imageUrl, long searchCount) {
        this.supplementId = supplementId;
        this.supplementName = supplementName;
        this.brandName = brandName;
        this.imageUrl = imageUrl;
        this.searchCount = searchCount;
    }

    // Tuple 결과를 DTO로 변환하는 정적 메소드
    public static PopularSupplementDTO from(Supplement supplement, long searchCount) {
        return PopularSupplementDTO.builder()
                .supplementId(supplement.getId())
                .supplementName(supplement.getName())
                .brandName(supplement.getBrand().getName())
                .imageUrl(supplement.getImageUrl())
                .searchCount(searchCount)
                .build();
    }
}

