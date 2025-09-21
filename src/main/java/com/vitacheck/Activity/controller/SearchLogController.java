package com.vitacheck.Activity.controller;

import com.vitacheck.Activity.dto.PopularIngredientDTO;
import com.vitacheck.Activity.dto.PopularSupplementDTO;
import com.vitacheck.Activity.service.SearchLogService;
import com.vitacheck.auth.config.jwt.CustomUserDetails;
import com.vitacheck.common.CustomResponse;
import com.vitacheck.common.enums.Gender;
import com.vitacheck.product.dto.SupplementResponseDTO;
import com.vitacheck.product.service.SupplementService;
import com.vitacheck.user.domain.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
public class SearchLogController {

    private final RedisTemplate<String, String> redisTemplate;
    private final SearchLogService searchLogService;
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
    public CustomResponse<List<SupplementResponseDTO.SimpleResponse>> getRecentProducts(
            @Parameter(description = "가져올 상품 개수") @RequestParam(defaultValue = "5") int limit
    ) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User user = null;

        if (authentication != null && authentication.getPrincipal() instanceof CustomUserDetails) {
            CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
            user = userDetails.getUser(); // User 객체를 꺼냅니다.
        }
        List<SupplementResponseDTO.SimpleResponse> recentProducts = searchLogService.findRecentProducts(user, limit);
        return CustomResponse.ok(recentProducts);
    }

    @Operation(summary = "인기 검색 성분 조회", description = "가장 많이 검색된 성분을 순서대로 조회합니다.")
    @GetMapping("/api/v1/ingredients/popular")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "인기 검색 성분 조회",
                    content = @Content(examples = @ExampleObject(value = "{\"isSuccess\":true,\"code\":\"COMMON200\",\"message\":\"성공적으로 요청을 수행했습니다.\",\"result\":\"FCM 토큰이 업데이트되었습니다.\"}"))),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자",
                    content = @Content(examples = @ExampleObject(value = "{\"isSuccess\":false,\"code\":\"U0001\",\"message\":\"로그인이 필요합니다.\",\"result\":null}"))),
            @ApiResponse(responseCode = "404", description = "사용자를 찾을 수 없음",
                    content = @Content(examples = @ExampleObject(value = "{\"isSuccess\":false,\"code\":\"U0002\",\"message\":\"사용자를 찾을 수 없습니다.\",\"result\":null}")))
    })
    public CustomResponse<List<PopularIngredientDTO>> getPopularIngredients(
            @Parameter(description = "조회할 연령대", required = true, example = "20대",
                    schema = @Schema(type = "string", allowableValues = {"10대", "20대", "30대", "40대", "50대", "60대 이상", "전체"}))
            @RequestParam String ageGroup,

            @Parameter(description = "상위 몇 개까지 조회할지", example = "10")
            @RequestParam(defaultValue = "10") int limit
    ) {
        List<PopularIngredientDTO> result = searchLogService.findPopularIngredients(ageGroup, limit);
        return CustomResponse.ok(result);
    }

    @Operation(summary = "연령대/성별별 인기 영양제 조회", description = "검색 횟수를 기준으로 연령대별 인기 영양제 순위를 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "연령대별 인기 영양제 조회 성공",
                    content = @Content(examples = @ExampleObject(value = "{\"isSuccess\":true,\"code\":\"COMMON200\",\"message\":\"성공적으로 요청을 수행했습니다.\",\"result\":\"FCM 토큰이 업데이트되었습니다.\"}"))),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자",
                    content = @Content(examples = @ExampleObject(value = "{\"isSuccess\":false,\"code\":\"U0001\",\"message\":\"로그인이 필요합니다.\",\"result\":null}"))),
            @ApiResponse(responseCode = "404", description = "사용자를 찾을 수 없음",
                    content = @Content(examples = @ExampleObject(value = "{\"isSuccess\":false,\"code\":\"U0002\",\"message\":\"사용자를 찾을 수 없습니다.\",\"result\":null}"))),
            @ApiResponse(responseCode = "400", description = "잘못된 연령대 형식 또는 지원하지 않는 연령대", content = @Content)

    })
    @Parameters({
            @Parameter(name = "ageGroup", description = "조회할 연령대", example = "20대",
                    schema = @Schema(type = "string", allowableValues = {"10대", "20대", "30대", "40대", "50대", "60대 이상", "전체"})),
            @Parameter(name = "gender", description = "조회할 성별", example = "MALE",
                    schema = @Schema(type = "string", allowableValues = {"MALE", "FEMALE", "전체"})), // ✅ Gender 파라미터 명세 추가
            @Parameter(name = "page", description = "페이지 번호 (0부터 시작)", example = "0"),
            @Parameter(name = "size", description = "한 페이지에 보여줄 아이템 수", example = "10"),
            @Parameter(name = "sort", hidden = true)
    })
    @GetMapping("/popular-supplements")
    public CustomResponse<Page<PopularSupplementDTO>> getPopularSupplements(
            @RequestParam(defaultValue = "전체") String ageGroup,
            @RequestParam(defaultValue = "전체") String gender,
            @Parameter(hidden = true) Pageable pageable
    ) {
        Gender genderEnum = "전체".equalsIgnoreCase(gender) ? Gender.NONE : Gender.valueOf(gender.toUpperCase());

        Page<PopularSupplementDTO> result = searchLogService.findPopularSupplements(ageGroup, genderEnum, pageable);
        return CustomResponse.ok(result);
    }
}
