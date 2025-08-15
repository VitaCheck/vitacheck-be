package com.vitacheck.controller;

import com.vitacheck.config.jwt.CustomUserDetails;
import com.vitacheck.domain.user.User;
import com.vitacheck.dto.CombinationDTO;
import com.vitacheck.global.apiPayload.CustomException;
import com.vitacheck.global.apiPayload.CustomResponse;
import com.vitacheck.global.apiPayload.code.ErrorCode;
import com.vitacheck.service.CombinationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@Tag(name="combination", description = "조합 관련 API")
@RestController
@RequestMapping("/api/v1/combinations")
@RequiredArgsConstructor
public class CombinationController {

    private final CombinationService combinationService;

    @Operation(summary = "조합 결과 확인", description = "영양제 조합 결과를 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공")
    })
    @PostMapping("/analyze") //
    public CustomResponse<CombinationDTO.AnalysisResponse> analyzeSupplementCombinations(
            @RequestBody CombinationDTO.AnalysisRequest request){

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User user = null;

        if (authentication != null && authentication.getPrincipal() instanceof CustomUserDetails) {
            CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
            user = userDetails.getUser(); // User 객체를 꺼냅니다.
        }

        if (request.getSupplementIds() == null || request.getSupplementIds().isEmpty()) {
            throw new CustomException(ErrorCode.SUPPLEMENT_LIST_EMPTY);
        }

        CombinationDTO.AnalysisResponse response = combinationService.analyze(user, request);
        return CustomResponse.ok(response);
    }

    @GetMapping("/recommend")
    @Operation(summary = "추천 조합 목록 조회", description = "궁합이 좋거나 주의가 필요한 조합 정보 전체를 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공")
    })
    public CustomResponse<CombinationDTO.RecommendCombinationResponse> getRecommendCombinations() {
        CombinationDTO.RecommendCombinationResponse response = combinationService.getRecommendCombinations();
        return CustomResponse.ok(response);
    }
}