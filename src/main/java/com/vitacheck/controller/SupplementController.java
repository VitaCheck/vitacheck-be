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

    @GetMapping("/search-cursor")
    @Operation(
            summary = "영양제 제품 검색 API (Cursor 기반 페이지네이션) By 박지영",
            description = """
                    키워드와 연관된 모든 영양제 제품이 조회됩니다. (성분, 브랜드, 제품 이름 등)
                    
                    ex. 유산균 → 제품 이름에 유산균 포함된 영양제, 유산균을 성분으로 포함하는 영양제 등 모두 조회됩니다.
                    
                    검색 키워드로 제품을 cursor 기반 페이지네이션으로 조회, 정렬은 인기도 높은 순으로(인기도=클릭수+검색수) 
                    
                    cursor 처음은 빈칸인채로 호출하면 됩니다. 그 이후 부터는 nextCursor 값이 아닌 '마지막으로 조회된 cursorID'를 넣어주면 됩니다. 
                    
                    nextcursor가 null이면 다음 페이지가 없다는 뜻입니다."""
    )
    public CustomResponse<SupplementDto.KeywordSearchSupplementBasedCursor> searchSupplements(
            @Parameter(name = "keyword", description = "검색 키워드", example = "유산균")
            @RequestParam String keyword,
            @Parameter(name = "cursorId", description = "마지막 조회된 아이디 (인기도 *1000000 + supplementId)", example = "")
            @RequestParam(required = false) Long cursorId,
            @Parameter(name = "size", description = "가져올 데이터 개수", example = "40")
            @RequestParam(defaultValue = "40") int size
    ) {
        SupplementDto.KeywordSearchSupplementBasedCursor responseDto =
                supplementService.searchSupplements(keyword, cursorId,size);

        return CustomResponse.ok(responseDto);
    }




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