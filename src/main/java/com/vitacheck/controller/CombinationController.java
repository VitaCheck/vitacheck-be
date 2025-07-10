package com.vitacheck.controller;

import com.vitacheck.dto.CombinationDTO;
import com.vitacheck.service.CombinationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name="combination", description = "조합 관련 API")
@RestController
@RequestMapping("/api/v1/combinations")
@RequiredArgsConstructor
public class CombinationController {

    private final CombinationService combinationService;

    @Operation(summary = "조합 결과 확인", description = "영양제 조합 결과를 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "조회 성공",
                    content = @Content(schema = @Schema(implementation = CombinationDTO.AnalysisResponse.class))),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자", content = @Content),
            @ApiResponse(responseCode = "404", description = "사용자를 찾을 수 없음", content = @Content)
    })
    @PostMapping("")
    public ResponseEntity<CombinationDTO.AnalysisResponse>analyzeSupplementComboinations(
            @RequestBody CombinationDTO.AnalysisRequest request){
        CombinationDTO.AnalysisResponse response = combinationService.analyze(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/recommend")
    @Operation(summary = "추천 조합 목록 조회", description = "궁합이 좋거나 주의가 필요한 조합 정보 전체를 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공",
                    content = @Content(schema = @Schema(implementation = CombinationDTO.RecommendCombinationResponse.class)))
    })
    public ResponseEntity<CombinationDTO.RecommendCombinationResponse> getRecommendCombinations() {
        CombinationDTO.RecommendCombinationResponse response = combinationService.getRecommendCombinations();
        return ResponseEntity.ok(response);
    }



}
