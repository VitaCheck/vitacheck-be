package com.vitacheck.controller;

import com.vitacheck.global.apiPayload.CustomResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;

@RestController
@RequiredArgsConstructor
public class SearchController {

    private final RedisTemplate<String, String> redisTemplate;

    @Operation(
            summary = "인기 검색어 조회 API By 박지영",
            description = "검색 수로 정렬되어 검색 키워드(keyword)와 검색 수(score)가 최대 10개 반환됩니다."
    )
    @GetMapping("/api/v1/search/popular")
    public CustomResponse<List<Map<String, Object>>> getPopularSearchKeywords() {
        String key = "search";

        // Redis에서 상위 10개 인기 검색어 가져오기
        Set<ZSetOperations.TypedTuple<String>> result =
                redisTemplate.opsForZSet().reverseRangeWithScores(key, 0, 9);

        List<Map<String, Object>> popularList = new ArrayList<>();

        if (result != null) {
            for (ZSetOperations.TypedTuple<String> tuple : result) {
                Map<String, Object> item = new HashMap<>();
                item.put("keyword", tuple.getValue());
                item.put("score", tuple.getScore());
                popularList.add(item);
            }
        }

        return CustomResponse.ok(popularList);
    }
}
