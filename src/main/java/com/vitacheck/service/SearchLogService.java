package com.vitacheck.service;

import com.vitacheck.config.jwt.CustomUserDetails;
import com.vitacheck.domain.Supplement;
import com.vitacheck.domain.searchLog.Method;
import com.vitacheck.domain.searchLog.SearchCategory;
import com.vitacheck.domain.searchLog.SearchLog;
import com.vitacheck.domain.user.Gender;
import com.vitacheck.domain.user.User;
import com.vitacheck.dto.SupplementDto;
import com.vitacheck.repository.BrandRepository;
import com.vitacheck.repository.IngredientRepository;
import com.vitacheck.repository.SearchLogRepository;
import com.vitacheck.repository.SupplementRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.Duration;
import java.time.LocalDate;
import java.time.Period;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SearchLogService {

    private final SearchLogRepository searchLogRepository;
    private final IngredientRepository ingredientRepository;
    private final SupplementRepository supplementRepository;
    private final BrandRepository brandRepository;
    private final StringRedisTemplate redisTemplate; // 추가

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
        final String normalizedKeyword = keyword.replaceAll("\\s+", "");
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

        // Redis에 카운트 증가 (ZSET 사용)
//        String key = "search:" + category.name().toLowerCase();

        String key = "search";
        if (Boolean.FALSE.equals(redisTemplate.hasKey(key))) {
            // 1. Redis에 key가 없을 때 (처음 생성되는 순간)
            redisTemplate.opsForZSet().incrementScore(key, normalizedKeyword, 1.0); // 점수 1 증가
            redisTemplate.expire(key, Duration.ofDays(1)); // TTL 1일 설정
        } else {
            // 2. Redis에 key가 이미 존재할 때
            redisTemplate.opsForZSet().incrementScore(key, normalizedKeyword, 1.0); // 점수만 1 증가
        }


        // Redis 결과 조회
        Set<ZSetOperations.TypedTuple<String>> result =
                redisTemplate.opsForZSet().reverseRangeWithScores("search", 0, 9);
        System.out.println(result);

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

    @Transactional(readOnly = true)
    public List<String> findRecentSearches(User user, int limit) {
        Pageable pageable = PageRequest.of(0, limit);
        return searchLogRepository.findRecentKeywordsByUserId(user.getId(), pageable);
    }

    @Transactional(readOnly = true)
    public List<SupplementDto.SimpleResponse> findRecentProducts(User user, int limit) {
        // 1. 최신순으로 정렬된, 중복 없는 상품 이름 목록을 가져옵니다.
        Pageable pageable = PageRequest.of(0, limit);
        List<String> supplementNames = searchLogRepository.findRecentViewedSupplementNamesByUserId(user.getId(), pageable);

        if (supplementNames.isEmpty()) {
            return Collections.emptyList();
        }

        // 2. 이름 목록으로 Supplement 엔티티들을 한 번에 조회합니다.
        List<Supplement> supplements = supplementRepository.findAllByNameIn(supplementNames);

        // 3. 1번에서 가져온 최신순으로 다시 정렬하며 DTO로 변환합니다.
        Map<String, Supplement> supplementMap = supplements.stream()
                .collect(Collectors.toMap(Supplement::getName, s -> s));

        return supplementNames.stream()
                .map(supplementMap::get)
                .filter(Objects::nonNull)
                .map(SupplementDto.SimpleResponse::from)
                .collect(Collectors.toList());
    }

    public void recordSearchLog(String keyword) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()
                || authentication.getPrincipal().equals("anonymousUser")) {
            // 🔹 미로그인 사용자 로그
            logSearch(null, keyword, SearchCategory.KEYWORD, null, null);
        } else {
            CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
            User user = userDetails.getUser();

            LocalDate birthDate = user.getBirthDate();
            int age = Period.between(birthDate, LocalDate.now()).getYears();

            // 🔹 로그인 사용자 로그
            logSearch(user.getId(), keyword, SearchCategory.KEYWORD, age, user.getGender());
        }
    }
}