package com.vitacheck.Activity.service;

import com.querydsl.core.Tuple;
import com.vitacheck.Activity.domain.SearchLog.QSearchLog;
import com.vitacheck.Activity.dto.PopularIngredientDTO;
import com.vitacheck.Activity.dto.PopularSupplementDTO;
import com.vitacheck.Activity.repository.SupplementLikeRepository;
import com.vitacheck.common.code.ErrorCode;
import com.vitacheck.common.enums.Gender;
import com.vitacheck.common.exception.CustomException;
import com.vitacheck.Activity.domain.SearchLog.Method;
import com.vitacheck.Activity.domain.SearchLog.SearchCategory;
import com.vitacheck.Activity.domain.SearchLog.SearchLog;
import com.vitacheck.product.domain.Ingredient.Ingredient;
import com.vitacheck.product.domain.Supplement.Supplement;
import com.vitacheck.product.dto.SupplementResponseDTO;
import com.vitacheck.product.repository.BrandRepository;
import com.vitacheck.product.repository.IngredientRepository;
import com.vitacheck.product.repository.SupplementRepository;
import com.vitacheck.Activity.repository.SearchLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SearchLogService {

    private final SearchLogRepository searchLogRepository;
    private final IngredientRepository ingredientRepository;
    private final SupplementRepository supplementRepository;
    private final BrandRepository brandRepository;
//    private final StringRedisTemplate redisTemplate; // 추가
//    private final RedisTemplate<Object, Object> redisTemplate;
    private final RedisTemplate<String, String> redisTemplate;
    private final SupplementLikeRepository supplementLikeRepository;

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
    public List<String> findRecentSearches(Long userid, int limit) {
        Pageable pageable = PageRequest.of(0, limit);
        return searchLogRepository.findRecentKeywordsByUserId(userid, pageable);
    }

    @Transactional(readOnly = true)
    public List<SupplementResponseDTO.SimpleResponse> findRecentProducts(Long userId, int limit) {
        // 1. 최신순으로 정렬된, 중복 없는 상품 이름 목록을 가져옵니다.
        Pageable pageable = PageRequest.of(0, limit);
        List<String> supplementNames = searchLogRepository.findRecentViewedSupplementNamesByUserId(userId, pageable);

        if (supplementNames.isEmpty()) {
            return Collections.emptyList();
        }

        // 2. 이름 목록으로 Supplement 엔티티들을 한 번에 조회합니다.
        List<Supplement> supplements = supplementRepository.findAllByNameIn(supplementNames);

        // 3. 1번에서 가져온 최신순으로 다시 정렬하며 DTO로 변환합니다.
        Map<String, Supplement> supplementMap = supplements.stream()
                .collect(Collectors.toMap(Supplement::getName, s -> s, (first, second) -> first));

        return supplementNames.stream()
                .map(supplementMap::get)
                .filter(Objects::nonNull)
                .map(SupplementResponseDTO.SimpleResponse::from)
                .collect(Collectors.toList());
    }

    public void recordSearchLog(String keyword, Long userId, Integer age, Gender gender) {
        if (userId == null) {
            logSearch(null, keyword, SearchCategory.KEYWORD, age, gender);
        } else {
            logSearch(userId, keyword, SearchCategory.KEYWORD, age, gender);
        }
    }

    public List<PopularIngredientDTO> findPopularIngredients(String ageGroup, int limit) {
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
                    return new PopularIngredientDTO(
                            ingredient != null ? ingredient.getId() : null, // ✅ ID를 DTO에 포함시킵니다.
                            ingredientName,
                            searchCount
                    );
                })
                .collect(Collectors.toList());
    }



    public Page<PopularSupplementDTO> findPopularSupplements(String ageGroup, Gender gender, Pageable pageable) {
        // 1. 연령대 문자열을 숫자 범위로 변환
        Integer startAge = null;
        Integer endAge = null;

        if (!"전체".equals(ageGroup)) {
            if (ageGroup.equals("60대 이상")) {
                startAge = 60;
                endAge = 150; // 매우 넓은 범위로 설정
            } else if (ageGroup.contains("대")) {
                try {
                    int decade = Integer.parseInt(ageGroup.replace("대", ""));
                    startAge = decade;
                    endAge = decade + 9;
                } catch (NumberFormatException e) {
                    throw new CustomException(ErrorCode.AGEGROUP_NOT_FOUND);
                }
            } else {
                throw new CustomException(ErrorCode.AGEGROUP_NOT_MATCHED);
            }
        }

        // 2. Repository 호출하여 Tuple 페이지를 받음
        Page<Tuple> resultPage = searchLogRepository.findPopularSupplements(startAge, endAge,gender, pageable);

        // 3. Tuple 페이지를 DTO 페이지로 변환
        return resultPage.map(tuple -> {
            Supplement supplement = tuple.get(0, Supplement.class);
            long searchCount = tuple.get(1, Long.class);
            return PopularSupplementDTO.from(supplement, searchCount);
        });
    }

}