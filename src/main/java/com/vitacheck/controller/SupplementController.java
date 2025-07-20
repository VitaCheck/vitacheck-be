package com.vitacheck.controller;

import com.vitacheck.dto.SupplementByPurposeResponse;
import com.vitacheck.dto.SupplementDto;
import com.vitacheck.dto.SupplementPurposeRequest;
import com.vitacheck.global.apiPayload.CustomException;
import com.vitacheck.global.apiPayload.CustomResponse;
import com.vitacheck.global.apiPayload.code.ErrorCode;
import com.vitacheck.service.SupplementService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

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

    @PostMapping("/by-purposes")
    @Operation(summary = "목적별 영양소 및 영양제 조회", description = "선택한 목적에 맞는 성분 및 관련 영양제를 반환합니다.")
    public ResponseEntity<Map<String, SupplementByPurposeResponse>> getSupplementsByPurposes(
            @RequestBody SupplementPurposeRequest request
    ) {
        return ResponseEntity.ok(supplementService.getSupplementsByPurposes(request));
    }
}