package com.vitacheck.service;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.vitacheck.config.jwt.CustomUserDetails;
import com.vitacheck.domain.*;
import com.vitacheck.domain.mapping.QIngredientAlternativeFood;
import com.vitacheck.domain.mapping.QSupplementIngredient;
import com.vitacheck.domain.searchLog.QSearchLog;
import com.vitacheck.domain.searchLog.SearchCategory;
import com.vitacheck.domain.user.Gender;
import com.vitacheck.domain.user.User;
import com.vitacheck.dto.IngredientResponseDTO;
import com.vitacheck.dto.PopularIngredientDto;
import com.vitacheck.common.exception.CustomException;
import com.vitacheck.common.code.ErrorCode;
import com.vitacheck.repository.IngredientAlternativeFoodRepository;
import com.vitacheck.repository.IngredientDosageRepository;
import com.vitacheck.repository.IngredientRepository;
import com.vitacheck.repository.SearchLogRepository;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import com.querydsl.core.Tuple;


import java.time.LocalDate;
import java.time.Period;
import java.util.ArrayList;
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
    private final SearchLogService searchLogService;
    private final RedisTemplate<Object, Object> redisTemplate;
    private final SearchLogRepository searchLogRepository;


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
        String dosageErrorCode = null;
        String foodErrorCode = null;


        // 1. 성분 가져오기
        Ingredient ingredient = ingredientRepository.findById(id)
                .orElseThrow(() -> new CustomException(ErrorCode.INGREDIENT_NOT_FOUND));

        Gender gender = Gender.NONE;
        int ageGroup = 0;

        // 2. 사용자 정보 가져오기 (로그인 여부에 따라 기본값 처리)
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();


        if (authentication == null || !authentication.isAuthenticated() || authentication.getPrincipal().equals("anonymousUser")) {
            // 로그인하지 않은 경우 기본값 설정
            dosageErrorCode = ErrorCode.UNAUTHORIZED.name();
            // 🔹 클릭 로그 저장 (미로그인)
            searchLogService.logClick(null, ingredient.getName(), SearchCategory.INGREDIENT, null,null);

        } else {
            CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
            User user = userDetails.getUser();
            gender = user.getGender();

            LocalDate birthDate = user.getBirthDate();
            int age = Period.between(birthDate, LocalDate.now()).getYears();
            ageGroup = (age / 10) * 10;

            // 🔹 클릭 로그 저장 (로그인)
            searchLogService.logClick(user.getId(), ingredient.getName(), SearchCategory.INGREDIENT, age, gender);

        }

        // 3. 섭취 기준 (dosage) 조회
        // 기본값 세팅 (-1이면 값 없다는 뜻)
        Double recommendedDosage = -1.0;
        Double upperLimit = -1.0;

        if (dosageErrorCode == null) {
            IngredientDosage dosage = ingredientDosageRepository.findBestDosage(id, gender, ageGroup)
                    .orElse(null);

            if (dosage == null) {
                dosageErrorCode = ErrorCode.INGREDIENT_DOSAGE_NOT_FOUND.name();
            } else {
                // 값은 있으나 필드가 null일 수 있는 경우 처리
                if (dosage.getRecommendedDosage() == null || dosage.getUpperLimit() == null) {
                    dosageErrorCode = ErrorCode.INGREDIENT_DOSAGE_HAVE_NULL.name();
                }
                recommendedDosage = dosage.getRecommendedDosage();
                upperLimit = dosage.getUpperLimit();
            }
        }


        // 4. 중간 테이블에서 대체 식품 ID 조회
        QIngredientAlternativeFood iaf = QIngredientAlternativeFood.ingredientAlternativeFood;
        List<Long> foodIds = queryFactory
                .select(iaf.alternativeFood.id)
                .from(iaf)
                .where(iaf.ingredient.id.eq(id))
                .fetch();

        // 5. 대체 식품 상세 정보 조회
        List<IngredientResponseDTO.SubIngredient> foodDTOs = new ArrayList<>();

        if (!foodIds.isEmpty()) {
                QAlternativeFood af = QAlternativeFood.alternativeFood;
                List<AlternativeFood> alternativeFoods = queryFactory
                        .selectFrom(af)
                        .where(af.id.in(foodIds))
                        .fetch();

                foodDTOs = alternativeFoods.stream()
                        .map(f -> IngredientResponseDTO.SubIngredient.builder()
                                .name(f.getName())
                                .imageOrEmoji(f.getEmoji())
                                .build())
                        .toList();
            } else {
                foodErrorCode = ErrorCode.INGREDIENT_FOOD_NOT_FOUND.name();
            }

            return IngredientResponseDTO.IngredientDetails.builder()
                    .id(ingredient.getId())
                    .name(ingredient.getName())
                    .description(ingredient.getDescription())
                    .effect(ingredient.getEffect())
                    .caution(ingredient.getCaution())
                    .age(ageGroup)
                    .gender(gender)
                    .recommendedDosage(recommendedDosage)
                    .upperLimit(upperLimit)
                    .subIngredients(foodDTOs)
                    .DosageErrorCode(dosageErrorCode)
                    .FoodErrorCode(foodErrorCode)
                    .build();

        }


    public IngredientResponseDTO.IngredientSupplementBasedCursor getIngredientSupplementBasedCursor(Long id, Long cursor, int size) {


    //1. 성분 이름으로 검색
        Ingredient ingredient = ingredientRepository.findById(id)
                .orElseThrow(() -> new CustomException(ErrorCode.INGREDIENT_NOT_FOUND));

        // 2. 관련 영양제 조회
        QSupplementIngredient si = QSupplementIngredient.supplementIngredient;
        QSupplement s = QSupplement.supplement;

        BooleanBuilder builder = new BooleanBuilder();

        //필수 조건
        builder.and(si.ingredient.id.eq(ingredient.getId()));
        //옵션
        if (cursor != null) {
            builder.and(s.id.gt(cursor));  // cursor가 있을 때만 조건 추가
        }

            List<IngredientResponseDTO.IngredientSupplement> supplements = queryFactory
                    .select(Projections.fields(
                            IngredientResponseDTO.IngredientSupplement.class,
                            s.id.as("id"),
                            s.name.as("name"),
                            s.coupangUrl.as("coupangUrl"),
                            s.imageUrl.as("imageUrl")
                    ))
                    .from(si)
                    .join(s).on(si.supplement.id.eq(s.id))
                    .where(builder)
                    .orderBy(s.id.asc())   // id 순서대로 정렬
                    .limit(size + 1)       // 다음 페이지가 있는지 확인하려고 size+1 가져오기
                    .fetch();

        // 다음 cursor 계산
        Long nextCursor = null;
        if (supplements.size() > size) {
            IngredientResponseDTO.IngredientSupplement last = supplements.remove(supplements.size() - 1);
            nextCursor = last.getId();
        }

        return IngredientResponseDTO.IngredientSupplementBasedCursor.builder()
                .supplements(supplements)
                .nextCursor(nextCursor)
                .build();
    }


    public List<PopularIngredientDto> findPopularIngredients(String ageGroup, int limit) {
        // 1. 연령대 문자열을 숫자 범위로 변환 (인기 영양제 로직과 동일)
        Integer startAge = null;
        Integer endAge = null;

        if (!"전체".equals(ageGroup)) {
            if (ageGroup.equals("60대 이상")) {
                startAge = 60;
                endAge = 150;
            } else if (ageGroup.contains("대")) {
                try {
                    int decade = Integer.parseInt(ageGroup.replace("대", ""));
                    startAge = decade;
                    endAge = decade + 9;
                } catch (NumberFormatException e) {
                    throw new IllegalArgumentException("올바른 연령대 형식이 아닙니다.");
                }
            } else {
                throw new IllegalArgumentException("지원하지 않는 연령대입니다.");
            }
        }

        // 2. Repository 호출하여 Tuple 리스트를 받음
        List<Tuple> results = searchLogRepository.findPopularIngredientsByAgeGroup(startAge, endAge, limit);

        // 3. Tuple 리스트를 DTO 리스트로 변환
        return results.stream()
                .map(tuple -> {
                    String ingredientName = tuple.get(QSearchLog.searchLog.keyword);
                    long searchCount = tuple.get(QSearchLog.searchLog.keyword.count());
                    // 성분 이름을 이용해 성분 객체를 찾아 ID를 가져옵니다. ✅
                    Ingredient ingredient = ingredientRepository.findByName(ingredientName)
                            .orElse(null);
                    return new PopularIngredientDto(
                            ingredient != null ? ingredient.getId() : null, // ✅ ID를 DTO에 포함시킵니다.
                            ingredientName,
                            searchCount
                    );
                })
                .collect(Collectors.toList());
    }

    }
