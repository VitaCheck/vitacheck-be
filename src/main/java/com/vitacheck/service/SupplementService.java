package com.vitacheck.service;

import com.vitacheck.config.jwt.CustomUserDetails;
import com.vitacheck.domain.Ingredient;
import com.vitacheck.domain.IngredientDosage;
import com.vitacheck.domain.Supplement;
import com.vitacheck.domain.mapping.SupplementIngredient;
import com.vitacheck.domain.purposes.AllPurpose;
import com.vitacheck.domain.purposes.PurposeCategory;
import com.vitacheck.domain.searchLog.SearchCategory;
import com.vitacheck.domain.user.User;
import com.vitacheck.dto.*;
import com.vitacheck.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.Period;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SupplementService {

    private final SupplementRepository supplementRepository;
    private final IngredientRepository ingredientRepository;
    private final PurposeCategoryRepository purposeCategoryRepository;
    private final SearchLogService searchLogService;
    private final StatisticsService statisticsService;
    private final SupplementLikeRepository supplementLikeRepository;
    private final IngredientDosageRepository dosageRepository;

    public SearchDto.UnifiedSearchResponse search(User user, String keyword, String brandName, String ingredientName, Pageable pageable) {

        List<SearchDto.IngredientInfo> matchedIngredients = findMatchedIngredients(keyword, ingredientName);

        Page<Supplement> supplementsPage = supplementRepository.search(user, keyword, brandName, ingredientName, pageable);
        Page<SupplementDto.SearchResponse> supplementDtos = supplementsPage.map(SupplementDto.SearchResponse::from);


        if (StringUtils.hasText(keyword)) {
            keyword=keyword;
        }
        else if (StringUtils.hasText(ingredientName)) {
            keyword=ingredientName;
        }
        else if (StringUtils.hasText(brandName)) {
            keyword=brandName;
        }

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated() || authentication.getPrincipal().equals("anonymousUser")) {
            // 🔹 검색 로그 저장(미로그인)
            searchLogService.logSearch(null, keyword, SearchCategory.KEYWORD, null,null);
        } else {
            CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
            user = userDetails.getUser();
            LocalDate birthDate = user.getBirthDate();
            int age = Period.between(birthDate, LocalDate.now()).getYears();
            // 🔹 검색 로그 저장(로그인)
            searchLogService.logSearch(user.getId(), keyword, SearchCategory.KEYWORD, age, user.getGender());
        }


        return SearchDto.UnifiedSearchResponse.builder()
                .matchedIngredients(matchedIngredients)
                .supplements(supplementDtos)
                .build();
    }


    private List<SearchDto.IngredientInfo> findMatchedIngredients(String keyword, String ingredientName) {
        // 1. 실제 검색에 사용할 검색어를 정합니다. ingredientName이 우선순위를 가집니다.
        String finalSearchTerm = StringUtils.hasText(ingredientName) ? ingredientName : keyword;

        // 2. 검색어가 존재하는 경우, 이름을 포함하는 모든 성분을 검색합니다.
        if (StringUtils.hasText(finalSearchTerm)) {
            return ingredientRepository.findByNameContainingIgnoreCase(finalSearchTerm).stream()
                    .map(SearchDto.IngredientInfo::from)
                    .collect(Collectors.toList());
        }

        // 3. 검색어가 없으면 빈 리스트를 반환합니다.
        return Collections.emptyList();
    }

    @Transactional(readOnly = true)
    public Map<String, SupplementByPurposeResponse> getSupplementsByPurposes(SupplementPurposeRequest request) {
        List<AllPurpose> allPurposes = request.getPurposeNames().stream()
                .map(AllPurpose::valueOf)
                .toList();

        // 1. 목적(Purpose)으로 PurposeCategory 엔티티를 조회합니다.
        List<PurposeCategory> categories = purposeCategoryRepository.findAllByNameIn(allPurposes);

        // 결과를 담을 Map을 생성합니다.
        Map<String, SupplementByPurposeResponse> result = new HashMap<>();

        // 2. 각 PurposeCategory를 순회합니다.
        for (PurposeCategory category : categories) {

            // 3. category.getIngredients()를 통해 직접 성분(Ingredient) 목록에 접근합니다.
            for (Ingredient ingredient : category.getIngredients()) {

                // 4. 각 성분에 연결된 영양제 정보를 가져옵니다.
                List<List<String>> supplementInfo = ingredient.getSupplementIngredients().stream()
                        .map(si -> si.getSupplement())
                        .map(supplement -> List.of(supplement.getName(), supplement.getImageUrl()))
                        .toList();

                // 5. 목적(Purpose) 목록을 가져옵니다.
                List<String> purposes = ingredient.getPurposeCategories().stream()
                        .map(pc -> pc.getName().getDescription())
                        .toList();

                // 6. 최종 결과 Map에 담습니다.
                result.put(ingredient.getName(),
                        SupplementByPurposeResponse.builder()
                                .purposes(purposes)
                                .supplements(supplementInfo)
                                .build());
            }
        }
        return result;
    }

    @Transactional(readOnly = true)
    public SupplementDetailResponseDto getSupplementDetail(Long supplementId, Long userId) {
        Supplement supplement = supplementRepository.findById(supplementId)
                .orElseThrow(() -> new IllegalArgumentException("해당 영양제를 찾을 수 없습니다."));

        boolean liked = (userId != null) && supplementLikeRepository.existsByUserIdAndSupplementId(userId, supplementId);

        return SupplementDetailResponseDto.builder()
                .supplementId(supplement.getId())
                .brandName(supplement.getBrand().getName())
                .brandImageUrl(supplement.getBrand().getImageUrl())
                .supplementName(supplement.getName())
                .supplementImageUrl(supplement.getImageUrl())
                .liked(liked)
                .coupangLink(supplement.getCoupangUrl())
                .intakeTime(supplement.getMethod())
                .ingredients(
                        supplement.getSupplementIngredients().stream()
                                .map(i -> {
                                    // Ingredient를 통해 unit을 가져오는 것이 아니라,
                                    // SupplementIngredient 자체의 unit을 사용하도록 수정
                                    String amount = (i.getAmount() != null) ? i.getAmount().toString() : "";
                                    // *** SupplementIngredient에 unit이 없으므로 Ingredient에서 가져와야 함 ***
                                    String unit = i.getIngredient().getUnit() != null ? i.getIngredient().getUnit() : "";

                                    return new SupplementDetailResponseDto.IngredientDto(
                                            i.getIngredient().getName(),
                                            amount + unit
                                    );
                                })
                                .toList())
                .build();
    }


    public List<SupplementDto.SimpleResponse> getSupplementsByBrandId(Long brandId) {
        // 이 메소드는 변경 없음
        return supplementRepository.findAllByBrandId(brandId).stream()
                .map(SupplementDto.SimpleResponse::from)
                .toList();
    }

    // 👇👇👇 [수정] getSupplementDetailById 메소드를 원래 로직으로 되돌립니다. 👇👇👇
    public SupplementDto.DetailResponse getSupplementDetailById(Long id) {
        Supplement supplement = supplementRepository.findByIdWithIngredients(id)
                .orElseThrow(() -> new RuntimeException("해당 영양제를 찾을 수 없습니다."));

        List<SupplementDto.DetailResponse.IngredientDetail> ingredients =
                supplement.getSupplementIngredients().stream()
                        .map(si -> {
                            Ingredient ingredient = si.getIngredient();
                            IngredientDosage dosage = dosageRepository.findGeneralDosageByIngredientId(ingredient.getId())
                                    .orElseThrow(() -> new RuntimeException("기준 정보 없음: " + ingredient.getName()));

                            double amount = si.getAmount() != null ? si.getAmount() : 0.0;
                            // dosage가 아닌 ingredient에서 unit을 가져오도록 수정
                            String unit = ingredient.getUnit() != null ? ingredient.getUnit() : "";

                            Double upperLimitOrNull = dosage.getUpperLimit();
                            double ul = (upperLimitOrNull != null) ? upperLimitOrNull : 0.0;

                            double percent = (ul > 0) ? (amount / ul) * 100.0 : 0.0;
                            percent = Math.min(percent, 999);

                            String status = percent < 30.0 ? "deficient"
                                    : percent <= 70.0 ? "in_range"
                                    : "excessive";

                            return SupplementDto.DetailResponse.IngredientDetail.builder()
                                    .id(ingredient.getId())
                                    .name(ingredient.getName())
                                    .amount(amount + unit)
                                    .status(status)
                                    .visualization(SupplementDto.DetailResponse.IngredientDetail.Visualization.builder()
                                            .normalizedAmountPercent(Math.round(percent * 10) / 10.0)
                                            .recommendedStartPercent(30.0)
                                            .recommendedEndPercent(70.0)
                                            .build())
                                    .build();
                        })
                        .toList();

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated() || authentication.getPrincipal().equals("anonymousUser")) {
            // 🔹 클릭 로그 저장 (미로그인)
            searchLogService.logClick(null, supplement.getName(), SearchCategory.SUPPLEMENT, null,null);

        } else {
            CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
            User user = userDetails.getUser();
            LocalDate birthDate = user.getBirthDate();
            int age = Period.between(birthDate, LocalDate.now()).getYears();

            // 🔹 클릭 로그 저장 (로그인)
            searchLogService.logClick(user.getId(), supplement.getName(), SearchCategory.SUPPLEMENT, age, user.getGender());
        }

        return SupplementDto.DetailResponse.builder()
                .supplementId(supplement.getId())
                .ingredients(ingredients)
                .build();
    }
}