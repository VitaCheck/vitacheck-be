package com.vitacheck.etc;

import com.vitacheck.common.enums.Gender;

import com.vitacheck.product.domain.Ingredient.Ingredient;
import com.vitacheck.product.domain.Ingredient.IngredientDosage;
import com.vitacheck.product.repository.IngredientDosageRepository;
import com.vitacheck.user.domain.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.Period;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DosageService {


    private final IngredientDosageRepository ingredientDosageRepository;

    /**
     * 사용자와 분석할 성분 목록을 기반으로, 각 성분별 최적의 섭취 기준을 조회하여 Map으로 반환합니다.
     */
    public Map<Long, IngredientDosage> getDosagesForUserAndIngredients(
            Gender gender, LocalDate birthDate, Set<Ingredient> ingredients) {

        if (gender == null || birthDate == null || ingredients.isEmpty()) {
            return Collections.emptyMap();
        }

        int age = Period.between(birthDate, LocalDate.now()).getYears();
        List<Long> ingredientIds = ingredients.stream().map(Ingredient::getId).collect(Collectors.toList());

        List<IngredientDosage> dosages = ingredientDosageRepository.findApplicableDosages(ingredientIds, gender, age);

        if (!dosages.isEmpty()) {
            dosages.forEach(d -> log.info(">> 조회된 dosage 정보: {}", d));
        }

        // 성분별로 가장 적합한 기준(성별 일치 > ALL)을 선택하여 Map으로 만듦
        return dosages.stream()
                .collect(Collectors.toMap(
                        dosage -> dosage.getIngredient().getId(),
                        dosage -> dosage,
                        (dosage1, dosage2) -> dosage1.getGender() != Gender.ALL ? dosage1 : dosage2
                ));
    }
}