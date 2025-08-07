package com.vitacheck.service;

import com.querydsl.jpa.impl.JPAQueryFactory;
import com.vitacheck.domain.AlternativeFood;
import com.vitacheck.domain.Ingredient;
import com.vitacheck.domain.IngredientDosage;
import com.vitacheck.domain.QAlternativeFood;
import com.vitacheck.domain.mapping.QIngredientAlternativeFood;
import com.vitacheck.domain.mapping.QSupplementIngredient;
import com.vitacheck.domain.user.Gender;
import com.vitacheck.domain.user.User;
import com.vitacheck.dto.IngredientResponseDTO;
import com.vitacheck.global.apiPayload.CustomException;
import com.vitacheck.global.apiPayload.code.ErrorCode;
import com.vitacheck.repository.IngredientAlternativeFoodRepository;
import com.vitacheck.repository.IngredientDosageRepository;
import com.vitacheck.repository.IngredientRepository;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.Period;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Getter
public class IngredientService {
    private final IngredientRepository ingredientRepository;
    private final IngredientAlternativeFoodRepository ingredientAlternativeFoodRepository;
    private final JPAQueryFactory queryFactory;
    private final IngredientDosageRepository ingredientDosageRepository;

    public List<IngredientResponseDTO.IngredientName> searchIngredientName(String keyword) {
        //1. 성분 이름으로 검색
        List<Ingredient> ingredients=ingredientRepository.findByNameContainingIgnoreCase(keyword);

        if (ingredients.isEmpty()) {
            throw new CustomException(ErrorCode.INGREDIENT_NOT_FOUND);
        }

        return ingredients.stream()
                .map(ingredient -> IngredientResponseDTO.IngredientName.builder()
                        .id(ingredient.getId())
                        .name(ingredient.getName())
                        .build())
                .collect(Collectors.toList());
    }

    public IngredientResponseDTO.IngredientDetails getIngredientDetails(Long id) {
        // 1. 성분 가져오기
        Ingredient ingredient = ingredientRepository.findById(id)
                .orElseThrow(() -> new CustomException(ErrorCode.INGREDIENT_NOT_FOUND));

        // 2. 사용자 정보 가져오기 (로그인 여부에 따라 기본값 처리)
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        Gender gender;
        int ageGroup;

        if (authentication == null || !authentication.isAuthenticated() || authentication.getPrincipal().equals("anonymousUser")) {
            // 로그인하지 않은 경우 기본값 설정
            gender = Gender.ALL;
            ageGroup = 30;

        } else {
            User user = (User) authentication.getPrincipal();
            gender = user.getGender();

            LocalDate birthDate = user.getBirthDate();
            int age = Period.between(birthDate, LocalDate.now()).getYears();
            ageGroup = (age / 10) * 10;


        }
        IngredientDosage dosage = ingredientDosageRepository.findBestDosage(id,gender,ageGroup)
                .orElseThrow(() -> new CustomException(ErrorCode.INGREDIENT_USER_DOSAGE_NOT_FOUND));


        // 4. 중간 테이블에서 대체 식품 ID 조회
        QIngredientAlternativeFood iaf = QIngredientAlternativeFood.ingredientAlternativeFood;
        List<Long> foodIds = queryFactory
                .select(iaf.alternativeFood.id)
                .from(iaf)
                .where(iaf.ingredient.id.eq(id))
                .fetch();

        // 5. 대체 식품 상세 정보 조회
        if (foodIds.isEmpty()) {
            throw new CustomException(ErrorCode.INGREDIENT_FOOD_NOT_FOUND);
        }

        // 6. 대체 식품 조회
        QAlternativeFood af=QAlternativeFood.alternativeFood;
        List<AlternativeFood> alternativeFoods = queryFactory
                .selectFrom(af)
                .where(af.id.in(foodIds))
                .fetch();

        List<IngredientResponseDTO.SubIngredient> foodDTOs=alternativeFoods.stream()
                .map(f -> IngredientResponseDTO.SubIngredient.builder()
                        .name(f.getName())
                        .imageOrEmoji(f.getEmoji())
                        .build())
                .toList();

//        // 7. 관련 영양제 조회
//        QSupplementIngredient si=QSupplementIngredient.supplementIngredient;
//        List<>

        return IngredientResponseDTO.IngredientDetails.builder()
                .id(ingredient.getId())
                .name(ingredient.getName())
                .description(ingredient.getDescription())
                .effect(ingredient.getEffect())
                .caution(ingredient.getCaution())
                .age(ageGroup)
                .gender(dosage.getGender())
                .recommendedDosage(dosage.getRecommendedDosage())
                .upperLimit(dosage.getUpperLimit())
                .unit(ingredient.getUnit())
                .subIngredients(foodDTOs)
                .build();

    }

//    public IngredientResponseDTO.IngredientFood getAlternativeFoods(Long id) {
//        // 1. 성분 ID 조회
//        Ingredient ingredient = ingredientRepository.findById(id)
//                .orElseThrow(() -> new CustomException(ErrorCode.INGREDIENT_NOT_FOUND));

//        // 2. 중간 테이블에서 대체 식품 ID 조회
//        QIngredientAlternativeFood iaf = QIngredientAlternativeFood.ingredientAlternativeFood;
//        List<Long> foodIds = queryFactory
//                .select(iaf.alternativeFood.id)
//                .from(iaf)
//                .where(iaf.ingredient.id.eq(id))
//                .fetch();
//
//        // 3. 대체 식품 상세 정보 조회
//        if (foodIds.isEmpty()) {
//            throw new CustomException(ErrorCode.INGREDIENT_FOOD_NOT_FOUND);
//        }
//
//        // 4. 대체 식품 조회
//        QAlternativeFood af=QAlternativeFood.alternativeFood;
//        List<AlternativeFood> alternativeFoods = queryFactory
//                .selectFrom(af)
//                .where(af.id.in(foodIds))
//                .fetch();
//
//
//        List<IngredientResponseDTO.SubIngredient> foodDTOs=alternativeFoods.stream()
//                .map(f -> IngredientResponseDTO.SubIngredient.builder()
//                        .name(f.getName())
//                        .imageOrEmoji(f.getEmoji())
//                        .build())
//                .toList();
//
//        return IngredientResponseDTO.IngredientFood.builder()
//                .id(ingredient.getId())
//                .name(ingredient.getName())
//                .subIngredients(foodDTOs)
//                .build();
//    }

}
