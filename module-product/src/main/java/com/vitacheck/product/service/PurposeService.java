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

        List<Object[]> rows = purposeRepository.findPurposeWithLimitedSupplements(goalsEnum);

        // 목적명(String) -> (ingredientId -> rows)
        Map<String, Map<Long, List<Object[]>>> grouped = rows.stream()
                .collect(Collectors.groupingBy(
                        r -> (String) r[1],                       // ✅ purposeName (r[1])
                        Collectors.groupingBy(
                                r -> ((Number) r[2]).longValue()  // ✅ ingredientId (r[2])
                        )
                ));

        // DTO 변환
        return grouped.entrySet().stream()
                .map(purposeEntry -> {
                    String purposeName = purposeEntry.getKey();

                    List<IngredientResponseDTO.IngredientWithSupplement> ingredients =
                            purposeEntry.getValue().entrySet().stream()
                                    .map(ingredientEntry -> {
                                        Long ingredientId = ingredientEntry.getKey();
                                        String ingredientName = (String) ingredientEntry.getValue().get(0)[3]; // ✅ ingredientName (r[3])

                                        List<SupplementResponseDTO.SupplementInfo> supplements =
                                                ingredientEntry.getValue().stream()
                                                        .map(r -> SupplementResponseDTO.SupplementInfo.builder()
                                                                .id(((Number) r[4]).longValue())   // ✅ supplementId (r[4])
                                                                .name((String) r[5])               // ✅ supplementName (r[5])
                                                                .coupangUrl((String) r[6])         // ✅ coupang_url (r[6])
                                                                .imageUrl((String) r[7])           // ✅ image_url (r[7])
                                                                .build()
                                                        )
                                                        .toList();

                                        return IngredientResponseDTO.IngredientWithSupplement.builder()
                                                .ingredientId(ingredientId)
                                                .ingredientName(ingredientName)
                                                .supplementInfos(supplements)
                                                .build();
                                    })
                                    .toList();

                    return PurposeResponseDTO.PurposeWithIngredientSupplement.builder()
                            .name(purposeName)
                            .ingredients(ingredients)
                            .build();
                })
                .toList();
    }



}

