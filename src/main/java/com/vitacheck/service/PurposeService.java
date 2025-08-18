package com.vitacheck.service;

import com.querydsl.jpa.impl.JPAQueryFactory;
import com.vitacheck.domain.purposes.AllPurpose;
import com.vitacheck.dto.AllPurposeDto;
import com.vitacheck.dto.IngredientResponseDTO;
import com.vitacheck.dto.PurposeResponseDTO;
import com.vitacheck.repository.PurposeCategoryRepository;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
@Getter
public class PurposeService {
    private final JPAQueryFactory queryFactory;
    private final PurposeCategoryRepository purposeCategoryRepository;

    public List<AllPurposeDto> getAllPurposes() {
        return List.of(AllPurpose.values()).stream()
                .map(purpose -> new AllPurposeDto(purpose.name(), purpose.getDescription()))
                .collect(Collectors.toList());
    }

    public List<PurposeResponseDTO.PurposeWithIngredientSupplement> findByGoals(List<AllPurpose> goalsEnum) {
        List<String> goals = goalsEnum.stream()
                .map(Enum::name)   // Enum → "BONE"
                .toList(); // Enum -> String ("BONE", "IMMUNE" ...) .toList();
        List<Object[]> rows = purposeCategoryRepository.findPurposeWithLimitedSupplements(goals);

        Map<AllPurpose, Map<Long, List<Object[]>>> grouped = rows.stream()
                .collect(Collectors.groupingBy(
                        r -> AllPurpose.valueOf((String) r[0]),      // pc.name (Enum으로 변환)
                        Collectors.groupingBy(
                                r -> ((Number) r[1]).longValue()         // ingredientId
                        )
                ));

        List<PurposeResponseDTO.PurposeWithIngredientSupplement> results = grouped.entrySet().stream()
                .map(purposeEntry -> {
                    AllPurpose purpose = purposeEntry.getKey();

                    List<IngredientResponseDTO.IngredientWithSupplement> ingredients = purposeEntry.getValue().entrySet().stream()
                            .map(ingredientEntry -> {
                                Long ingredientId = ingredientEntry.getKey();
                                String ingredientName = (String) ingredientEntry.getValue().get(0)[2]; // ingredientName

                                List<IngredientResponseDTO.IngredientSupplement> supplements = ingredientEntry.getValue().stream()
                                        .map(r -> new IngredientResponseDTO.IngredientSupplement(
                                                ((Number) r[3]).longValue(),   // supplementId
                                                (String) r[4],                 // supplementName
                                                (String) r[5],                 // coupang_url
                                                (String) r[6]                  // image_url
                                        ))
                                        .toList();

                                return new IngredientResponseDTO.IngredientWithSupplement(
                                        ingredientId, ingredientName, supplements
                                );
                            })
                            .toList();

                    return new PurposeResponseDTO.PurposeWithIngredientSupplement(purpose, ingredients);
                })
                .toList();
        return results;
    }

}
