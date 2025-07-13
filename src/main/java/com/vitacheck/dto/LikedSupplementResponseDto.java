package com.vitacheck.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LikedSupplementResponseDto {

    private Long supplementId;
    private String name;
    private String brandName;
    private String imageUrl;
    private Integer price;
}
