package com.vitacheck.service;

import com.vitacheck.domain.searchLog.Method;
import com.vitacheck.domain.searchLog.SearchCategory;
import com.vitacheck.domain.searchLog.SearchLog;
import com.vitacheck.domain.user.Gender;
import com.vitacheck.domain.user.User;
import com.vitacheck.repository.BrandRepository;
import com.vitacheck.repository.IngredientRepository;
import com.vitacheck.repository.SearchLogRepository;
import com.vitacheck.repository.SupplementRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class SearchLogService {

    private final SearchLogRepository searchLogRepository;
    private final IngredientRepository ingredientRepository;
    private final SupplementRepository supplementRepository;
    private final BrandRepository brandRepository;

    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void record(Long userId,                 // 미로그인: null
                       String keyword,            // 필수
                       SearchCategory category,   // KEYWORD / INGREDIENT / BRAND / SUPPLEMENT
                       Method method,          // SEARCH / CLICK / LIKE
                       Integer age,               // 미로그인 시 null
                       Gender gender) {           // MALE, FEMALE, NONE, ALL / 미로그인 시 NONE


        // ---- 필수값 검증 ----
        if (!StringUtils.hasText(keyword)) {
            throw new IllegalArgumentException("keyword is required");
        }
        Objects.requireNonNull(method, "method is required");
        Objects.requireNonNull(category, "category is required");


        // ---- 정규화 ----
        final String normalizedKeyword = keyword.trim();
        final Gender normalizedGender = (gender != null) ? gender : Gender.NONE;


        // ---- 저장 ----
        SearchLog log = SearchLog.builder()
                .userId(userId)
                .keyword(normalizedKeyword)
                .category(category)
                .method(method)
                .age(age)
                .gender(normalizedGender)
                .build();

        searchLogRepository.save(log);
    }

    /** 검색 로그 */
    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void logSearch(Long userId,
                          String keyword,           // 검색어 (필수)
                          SearchCategory category,  // INGREDIENT, BRAND, SUPPLEMENT, KEYWORD
                          Integer age,              // null 가능
                          Gender gender) {          // null 가능 → NONE 처리

        // ---- 필수값 검증 ----
        if (!StringUtils.hasText(keyword)) {
            throw new IllegalArgumentException("keyword is required");
        }
        Objects.requireNonNull(category, "category is required");

        // ---- 정규화 ----
        final String normalizedKeyword = keyword.trim();
        final Gender normalizedGender = (gender != null) ? gender : Gender.NONE;

        String found = "false";

        switch (category) {
            case INGREDIENT:
                found = ingredientRepository.existsByName(normalizedKeyword);
                break;
            case BRAND:
                found = brandRepository.existsByName(normalizedKeyword);
                break;
            case SUPPLEMENT:
                found = supplementRepository.existsByName(normalizedKeyword);
                break;
            default:
                // KEYWORD 입력 → 순차 검색
                found = ingredientRepository.existsByName(normalizedKeyword);
                if (found != "false") {
                    category = SearchCategory.INGREDIENT;
                } else if ((found = brandRepository.existsByName(normalizedKeyword)) != "false") {
                    category = SearchCategory.BRAND;
                } else if ((found = supplementRepository.existsByName(normalizedKeyword)) != "false") {
                    category = SearchCategory.SUPPLEMENT;
                }
                break;
        }

        if (found=="false") {  // 이제 boolean이라 정상
            category = SearchCategory.KEYWORD;
        }

        record(userId, normalizedKeyword, category, Method.SEARCH, age, normalizedGender);
    }

    /** 클릭 로그: 어떤 카테고리의 무엇을 클릭했는지 명확히 전달 */
    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void logClick(Long userId,
                         String clickedText,            // 클릭된 라벨/이름(필수)
                         SearchCategory category,       // 클릭 대상의 카테고리
                         Integer age,
                         Gender gender) {

        // 클릭은 method가 고정이므로 내부에서 지정
        record(userId, clickedText, category, Method.CLICK, age, gender);
    }
}