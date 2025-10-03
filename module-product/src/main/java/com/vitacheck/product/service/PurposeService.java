package com.vitacheck.product.service;


import com.querydsl.jpa.impl.JPAQueryFactory;
import com.vitacheck.product.domain.Purpose.AllPurpose;
import com.vitacheck.product.dto.IngredientResponseDTO;
import com.vitacheck.product.dto.SupplementResponseDTO;
import com.vitacheck.product.repository.PurposeRepository;
import com.vitacheck.product.dto.PurposeResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PurposeService {

    private final PurposeRepository purposeRepository;
    private final JPAQueryFactory queryFactory;

    // 모든 목적 DB에서 가져오기
    public List<PurposeResponseDTO.AllPurposeDTO> getAllPurposes() {
        return purposeRepository.findAll().stream()
                .map(purpose -> new PurposeResponseDTO.AllPurposeDTO(
                        purpose.getEnumCode().name(), // Enum 이름 (EYE, IMMUNE 등)
                        purpose.getName()             // DB의 name 컬럼 값 (한글 설명)
                ))
                .collect(Collectors.toList());
    }
    public List<PurposeResponseDTO.PurposeWithIngredientSupplement> findByGoals(List<String> goalsEnum) {

        // 1. 목적에 맞는 성분 최대 3개
        List<Object[]> ingredientRows = purposeRepository.findTop3IngredientsByPurpose(goalsEnum);

        // 성분 ID 리스트 뽑기
        List<Long> ingredientIds = ingredientRows.stream()
                .map(r -> ((Number) r[0]).longValue()) // ingredientId
                .toList();

        // 2. 성분별 영양제 최대 10개
        List<Object[]> supplementRows = purposeRepository.findTop10SupplementsByIngredients(ingredientIds);

        // 보조: 성분 ID → 영양제 리스트 매핑
        Map<Long, List<SupplementResponseDTO.SupplementInfo>> supplementsByIngredient =
                supplementRows.stream()
                        .collect(Collectors.groupingBy(
                                r -> ((Number) r[4]).longValue(), // ingredientId (supplementRows의 다섯 번째 값)
                                Collectors.mapping(r -> SupplementResponseDTO.SupplementInfo.builder()
                                        .id(((Number) r[0]).longValue())    // supplementId
                                        .name((String) r[1])                // supplementName
                                        .coupangUrl((String) r[2])          // coupang_url
                                        .imageUrl((String) r[3])            // image_url
                                        .build(), Collectors.toList())
                        ));

        // 3. 목적별 그룹핑 후 DTO 변환
        Map<String, List<IngredientResponseDTO.IngredientWithSupplement>> ingredientsByPurpose =
                ingredientRows.stream()
                        .collect(Collectors.groupingBy(
                                r -> (String) r[3], // purposeName
                                Collectors.mapping(r -> {
                                    Long ingredientId = ((Number) r[0]).longValue();
                                    String ingredientName = (String) r[1];

                                    List<SupplementResponseDTO.SupplementInfo> supplements =
                                            supplementsByIngredient.getOrDefault(ingredientId, List.of());

                                    return IngredientResponseDTO.IngredientWithSupplement.builder()
                                            .ingredientId(ingredientId)
                                            .ingredientName(ingredientName)
                                            .supplementInfos(supplements)
                                            .build();
                                }, Collectors.toList())
                        ));

        // 최종 목적 DTO
        return ingredientsByPurpose.entrySet().stream()
                .map(entry -> PurposeResponseDTO.PurposeWithIngredientSupplement.builder()
                        .name(entry.getKey()) // purposeName
                        .ingredients(entry.getValue())
                        .build())
                .toList();
    }


}

