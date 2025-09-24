package com.vitacheck.Activity.dto;

import lombok.*;

@Data
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LikeToggleResponseDto {

    private Long supplementId;

    private boolean liked;
}
