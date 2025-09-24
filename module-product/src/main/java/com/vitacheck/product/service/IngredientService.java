package com.vitacheck.product.service;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.vitacheck.common.code.ErrorCode;
import com.vitacheck.common.enums.Gender;
import com.vitacheck.common.exception.CustomException;
import com.vitacheck.product.domain.Ingredient.*;
import com.vitacheck.product.domain.Supplement.QSupplement;
import com.vitacheck.product.domain.Supplement.QSupplementIngredient;
import com.vitacheck.product.dto.IngredientResponseDTO;
import com.vitacheck.product.dto.SupplementResponseDTO;
import com.vitacheck.product.repository.IngredientDosageRepository;
import com.vitacheck.product.repository.IngredientRepository;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDate;
import java.time.Period;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Getter
public class IngredientService {
    private final IngredientRepository ingredientRepository;
    private final JPAQueryFactory queryFactory;
    private final IngredientDosageRepository ingredientDosageRepository;
//    private final JwtParser jwtParser; // 토큰에서 email 추출용
//    private final UserPort userPort;   // user 모듈의 구현체(UserAdapter)가 주입됨



//    public UserInfoDTO getUserInfo(String token) {
//        String email = jwtParser.getEmail(token);
//        return userPort.getUserInfoByEmail(email);
//    }


    // 성분 이름으로 검색하여 매칭된 성분 가져오기
    public List<IngredientResponseDTO.IngredientName> searchIngredientName(String keyword) {
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

//    // 유저 정보에 따라 성분 권장량과 상한 정보 가져오기
//    public Optional<IngredientDosage> findBestDosageForUser(Long ingredientId, User user) {
//        if (user == null || user.getBirthDate() == null) {
//            return Optional.empty();
//        }
//        int age = Period.between(user.getBirthDate(), LocalDate.now()).getYears();
//        return ingredientRepository.findBestDosage(ingredientId, user.getGender(), age);
//    }
    // 유저 정보에 따라 성분 권장량과 상한 정보 가져오기 (변경 후)
//    public Optional<IngredientDosage> findBestDosageForUser(Long ingredientId, UserInfoDTO userInfo) {
//        if (userInfo == null || userInfo.age() == null) {
//            return Optional.empty();
//        }
//        return ingredientRepository.findBestDosage(ingredientId, userInfo.gender(), userInfo.age()
//        );
//    }

    // 성분 정보(설명, 대체식품, 권장량 등) 가져오기
    public IngredientResponseDTO.IngredientDetails getIngredientDetails(Long id) {
        {
            String dosageErrorCode = null;
            String foodErrorCode = null;


            // 1. 성분 가져오기
            Ingredient ingredient = ingredientRepository.findById(id)
                    .orElseThrow(() -> new CustomException(ErrorCode.INGREDIENT_NOT_FOUND));

            // 2. 사용자 정보 가져오기 (로그인 여부에 따라 기본값 처리)

//            UserInfoDTO userInfo = getUserInfo(token);
//            Gender gender = (userInfo != null) ? userInfo.gender() : Gender.NONE;
//            int ageGroup = (userInfo != null && userInfo.age() != null) ? (userInfo.age() / 10) * 10 : -1;
            Gender gender=Gender.MALE;
            int ageGroup=20;

//        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
//
//        if (authentication == null || !authentication.isAuthenticated() || authentication.getPrincipal().equals("anonymousUser")) {
//            // 로그인하지 않은 경우 기본값 설정
//            dosageErrorCode = ErrorCode.UNAUTHORIZED.name();
//////            // 🔹 클릭 로그 저장 (미로그인)
//////            searchLogService.logClick(null, ingredient.getName(), SearchCategory.INGREDIENT, null,null);
//
//        } else {
//            CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
////            User user = userDetails.getUser();
////            gender = user.getGender();
////
////            LocalDate birthDate = user.getBirthDate();
////            int age = Period.between(birthDate, LocalDate.now()).getYears();
////            ageGroup = (age / 10) * 10;
////
//////            // 🔹 클릭 로그 저장 (로그인)
//////            searchLogService.logClick(user.getId(), ingredient.getName(), SearchCategory.INGREDIENT, age, gender);
////
//        }


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
    }

        public IngredientResponseDTO.IngredientSupplementBasedCursor getIngredientSupplementBasedCursor (Long id, Long
        cursor,int size){


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

            List<SupplementResponseDTO.SupplementInfo> supplements = queryFactory
                    .select(Projections.fields(
                            SupplementResponseDTO.SupplementInfo.class,
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
                SupplementResponseDTO.SupplementInfo last = supplements.remove(supplements.size() - 1);
                nextCursor = last.getId();
            }

            return IngredientResponseDTO.IngredientSupplementBasedCursor.builder()
                    .supplementInfos(supplements)
                    .nextCursor(nextCursor)
                    .build();
        }
}
