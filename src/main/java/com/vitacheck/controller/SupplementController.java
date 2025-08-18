package com.vitacheck.controller;

import com.vitacheck.domain.user.Gender;
import com.vitacheck.domain.user.User;
import com.vitacheck.dto.*;
import com.vitacheck.global.apiPayload.CustomException;
import com.vitacheck.global.apiPayload.CustomResponse;
import com.vitacheck.global.apiPayload.code.ErrorCode;
import com.vitacheck.service.StatisticsService;
import com.vitacheck.service.SupplementService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/supplements")
@Slf4j

public class SupplementController {

    private final SupplementService supplementService;
    private final StatisticsService statisticsService;

    @GetMapping("/search")
    @Operation(summary = "영양제 통합 검색", description = "키워드, 브랜드명, 성분명으로 영양제를 검색합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "검색 성공"),
            @ApiResponse(responseCode = "400", description = "검색 파라미터가 없는 경우")
    })
    public CustomResponse<SearchDto.UnifiedSearchResponse> searchSupplements(
            @AuthenticationPrincipal User user,
            @Parameter(description = "검색 키워드 (상품명)") @RequestParam(required = false) String keyword,
            @Parameter(description = "브랜드 이름") @RequestParam(required = false) String brandName,
            @Parameter(description = "성분 이름") @RequestParam(required = false) String ingredientName,
            @ParameterObject Pageable pageable
    ) {
        if (!StringUtils.hasText(keyword) && !StringUtils.hasText(brandName) && !StringUtils.hasText(ingredientName)) {
            throw new CustomException(ErrorCode.SEARCH_KEYWORD_EMPTY);
        }

        SearchDto.UnifiedSearchResponse response = supplementService.search(user, keyword, brandName, ingredientName, pageable);
        return CustomResponse.ok(response);
    }

//    @PostMapping("/by-purposes")
//    @Operation(summary = "목적별 영양소 및 영양제 조회", description = "선택한 목적에 맞는 성분 및 관련 영양제를 반환합니다.")
//    @ApiResponses({
//            @ApiResponse(responseCode = "200", description = "조회 성공")
//    })
//    public CustomResponse<Map<String, SupplementByPurposeResponse>> getSupplementsByPurposes(
//            @RequestBody SupplementPurposeRequest request
//    ) {
//        Map<String, SupplementByPurposeResponse> response = supplementService.getSupplementsByPurposes(request);
//        return CustomResponse.ok(response);
//    }

//    @PostMapping("/by-purposes")
//    @Operation(summary = "목적별 영양소·영양제 조회(페이징)")
//    // ⭐️ 반환 타입을 Map<String, Object> 로 변경합니다.
//    public Map<String, Object> getSupplementsByPurposes(
//            @RequestBody SupplementPurposeRequest request,
//            @org.springdoc.core.annotations.ParameterObject Pageable pageable
//    ) {
//        log.info("[Controller] page={}, size={}, sort={}",
//                pageable.getPageNumber(), pageable.getPageSize(), pageable.getSort());
//
//        // 1. 서비스에서 Slice 데이터를 받아옵니다.
//        Slice<IngredientPurposeBucket> sliceData = supplementService.getSupplementsByPurposesPaged(request, pageable);
//
//        // 2. ⭐️ Map 객체를 직접 생성하여 JSON 구조를 수동으로 만듭니다.
//        Map<String, Object> response = new HashMap<>();
//        response.put("content", sliceData.getContent()); // 데이터 목록 추가
//        response.put("hasNext", sliceData.hasNext());   // 다음 페이지 여부 추가
//
//        // 3. ⭐️ 완성된 Map을 반환합니다.
//        return response;
//    }



    // 특정 영양제 상세 정보 반환 API
    @GetMapping
    @Operation(summary = "영양제 상세 조회", description = "supplementId로 상세 정보를 조회합니다.")
    public SupplementDetailResponseDto getSupplement(
            @RequestParam Long id,
            @RequestHeader(name = "X-User-Id", required = false) Long userId) {
        return supplementService.getSupplementDetail(id, userId);
    }

    // 특정 브랜드 다른 영양제 목록 반환 API
    @GetMapping("/brand")
    @Operation(summary = "특정 브랜드의 다른 영양제 목록 반환", description = "특정 브랜드의 다른 영양제 목록을 반환합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공")
    })
    public Map<String, List<SupplementDto.SimpleResponse>> getByBrandId(@RequestParam Long id) {
        List<SupplementDto.SimpleResponse> list = supplementService.getSupplementsByBrandId(id);
        return Map.of("supplements", list);
    }

    // 특정 영양제의 상세정보 반환 API DTO
    @GetMapping("/detail")
    @Operation(summary = "영양제 상세 조회", description = "성분별 함량, 상태, 시각화 정보 등을 반환합니다.")
    public SupplementDto.DetailResponse getSupplementDetail(@RequestParam Long id) {
        return supplementService.getSupplementDetailById(id);
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
    public CustomResponse<Page<PopularSupplementDto>> getPopularSupplements(
            @RequestParam(defaultValue = "전체") String ageGroup,
            @RequestParam(defaultValue = "전체") String gender,
            @Parameter(hidden = true) Pageable pageable
    ) {
        Gender genderEnum = "전체".equalsIgnoreCase(gender) ? Gender.NONE : Gender.valueOf(gender.toUpperCase());

        Page<PopularSupplementDto> result = supplementService.findPopularSupplements(ageGroup, genderEnum, pageable);
        return CustomResponse.ok(result);
    }
}