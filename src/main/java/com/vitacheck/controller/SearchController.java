package com.vitacheck.controller;

import com.vitacheck.config.jwt.CustomUserDetails;
import com.vitacheck.domain.user.User;
import com.vitacheck.dto.SupplementDto;
import com.vitacheck.common.CustomResponse;
import com.vitacheck.service.IngredientService;
import com.vitacheck.service.SearchLogService;
import com.vitacheck.service.SupplementService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;

@RestController
@RequiredArgsConstructor
public class SearchController {

    private final RedisTemplate<String, String> redisTemplate;
    private final SearchLogService searchLogService;
    private final IngredientService ingredientService;
    private final SupplementService supplementService;

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

    @Operation(
            summary = "검색 기록 API By 박지영",
            description = " 검색에서 검색한 키워드를 DB에 저장/기록 합니다. 모든 검색 기능에서 이 api를 같이 호출해주시면 됩니다."
    )
    @GetMapping("/api/v1/search/logs")
    public CustomResponse<Void> recordSearchLog(
            @Parameter(name = "keyword", description = "검색 키워드", example = "유산균")
            @RequestParam String keyword) {
        searchLogService.recordSearchLog(keyword);
        return CustomResponse.ok(null);
    }

    @Operation(summary = "최근 검색어 조회", description = "로그인한 사용자의 최근 검색어 목록을 중복 없이 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "프로필 URL 업데이트 성공",
                    content = @Content(examples = @ExampleObject(value = "{\"isSuccess\":true,\"code\":\"COMMON200\",\"message\":\"성공적으로 요청을 수행했습니다.\",\"result\":\"FCM 토큰이 업데이트되었습니다.\"}"))),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자",
                    content = @Content(examples = @ExampleObject(value = "{\"isSuccess\":false,\"code\":\"U0001\",\"message\":\"로그인이 필요합니다.\",\"result\":null}"))),
            @ApiResponse(responseCode = "404", description = "사용자를 찾을 수 없음",
                    content = @Content(examples = @ExampleObject(value = "{\"isSuccess\":false,\"code\":\"U0002\",\"message\":\"사용자를 찾을 수 없습니다.\",\"result\":null}")))
    })
    @GetMapping("/recent")
    public CustomResponse<List<String>> getRecentSearches(
            @Parameter(description = "가져올 검색어 개수") @RequestParam(defaultValue = "3") int limit
    ) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User user = null;

        if (authentication != null && authentication.getPrincipal() instanceof CustomUserDetails) {
            CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
            user = userDetails.getUser(); // User 객체를 꺼냅니다.
        }

        List<String> recentSearches = searchLogService.findRecentSearches(user, limit);
        return CustomResponse.ok(recentSearches);
    }

    @Operation(summary = "최근 본 상품 조회", description = "로그인한 사용자가 최근에 클릭한 상품 목록을 중복 없이 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "프로필 URL 업데이트 성공",
                    content = @Content(examples = @ExampleObject(value = "{\"isSuccess\":true,\"code\":\"COMMON200\",\"message\":\"성공적으로 요청을 수행했습니다.\",\"result\":\"FCM 토큰이 업데이트되었습니다.\"}"))),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자",
                    content = @Content(examples = @ExampleObject(value = "{\"isSuccess\":false,\"code\":\"U0001\",\"message\":\"로그인이 필요합니다.\",\"result\":null}"))),
            @ApiResponse(responseCode = "404", description = "사용자를 찾을 수 없음",
                    content = @Content(examples = @ExampleObject(value = "{\"isSuccess\":false,\"code\":\"U0002\",\"message\":\"사용자를 찾을 수 없습니다.\",\"result\":null}")))
    })
    @GetMapping("/me/recent-products")
    public CustomResponse<List<SupplementDto.SimpleResponse>> getRecentProducts(
            @Parameter(description = "가져올 상품 개수") @RequestParam(defaultValue = "5") int limit
    ) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User user = null;

        if (authentication != null && authentication.getPrincipal() instanceof CustomUserDetails) {
            CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
            user = userDetails.getUser(); // User 객체를 꺼냅니다.
        }
        List<SupplementDto.SimpleResponse> recentProducts = searchLogService.findRecentProducts(user, limit);
        return CustomResponse.ok(recentProducts);
    }
}
