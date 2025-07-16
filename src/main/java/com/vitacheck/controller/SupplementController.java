package com.vitacheck.controller;

import com.vitacheck.dto.SupplementDto;
import com.vitacheck.global.apiPayload.CustomException;
import com.vitacheck.global.apiPayload.CustomResponse;
import com.vitacheck.global.apiPayload.code.ErrorCode;
import com.vitacheck.service.SupplementService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/supplements")
public class SupplementController {

    private final SupplementService supplementService;

    @GetMapping("/search")
    @Operation(summary = "영양제 통합 검색", description = "키워드, 브랜드명, 성분명으로 영양제를 검색합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "검색 성공"),
            @ApiResponse(responseCode = "400", description = "검색 파라미터가 없는 경우")
    })
    public CustomResponse<List<SupplementDto.SearchResponse>> searchSupplements(
            @Parameter(description = "검색 키워드 (상품명)") @RequestParam(required = false) String keyword,
            @Parameter(description = "브랜드 이름") @RequestParam(required = false) String brandName,
            @Parameter(description = "성분 이름") @RequestParam(required = false) String ingredientName
    ) {
        if (!StringUtils.hasText(keyword) && !StringUtils.hasText(brandName) && !StringUtils.hasText(ingredientName)) {
            throw new CustomException(ErrorCode.SEARCH_KEYWORD_EMPTY);
        }

        List<SupplementDto.SearchResponse> response = supplementService.search(keyword, brandName, ingredientName);

        return CustomResponse.ok(response);
    }
}