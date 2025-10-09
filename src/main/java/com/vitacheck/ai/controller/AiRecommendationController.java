package com.vitacheck.ai.controller;

import com.vitacheck.ai.dto.AiRecommendationRequestDto;
import com.vitacheck.ai.dto.AiRecommendationResponseDto;
import com.vitacheck.ai.service.GeminiRecommendationService;
import com.vitacheck.common.CustomResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.io.IOException;

@Tag(name = "AI Recommendation", description = "🤖 Gemini 기반 영양제 조합 추천 API")
@RestController
@RequestMapping("/api/v1/ai")
@RequiredArgsConstructor
public class AiRecommendationController {

    private final GeminiRecommendationService recommendationService;

    @PostMapping("/recommendations/combinations")
    @Operation(
            summary = "AI 영양제 조합 추천",
            description = """
        사용자가 선택한 **최대 3개**의 건강 목적에 가장 적합한 영양제 조합을 AI가 추천합니다.
        
        **동작 방식:**
        1. 사용자가 '눈 건강', '피로 개선' 등 건강 목적을 1~3개 선택하여 요청합니다.
        2. AI(Gemini)가 전체 영양제 데이터를 분석하여, 선택된 목적들을 종합적으로 관리할 수 있는 최적의 조합을 찾습니다.
        3. 성분 중복을 피하고 시너지를 고려한 여러 조합을 추천 이유와 함께 제시합니다.
        """
    )
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "사용자의 건강 목적 목록을 전달합니다.",
            required = true,
            content = @Content(
                    schema = @Schema(implementation = AiRecommendationRequestDto.class),
                    examples = @ExampleObject(
                            name = "AI 조합 추천 요청 예시",
                            value = "{\"purposes\": [\"눈 건강\", \"피로감\"]}"
                    )
            )
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "AI 추천 조합 조회 성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = CustomResponse.class),
                            examples = @ExampleObject(
                                    name = "조합 추천 성공 응답",
                                    value = """
                    {
                      "isSuccess": true,
                      "code": "COMMON200",
                      "message": "성공적으로 요청을 수행했습니다.",
                      "result": {
                        "recommendedCombinations": [
                          {
                            "combinationName": "눈 피로 집중 케어 조합",
                            "supplementIds": [15, 22],
                            "reason": "눈 건강의 핵심인 루테인과 피로 회복을 돕는 비타민 B군을 함께 섭취하여 시너지 효과를 낼 수 있는 조합입니다."
                          },
                          {
                            "combinationName": "종합 활력 부스트 조합",
                            "supplementIds": [1, 35, 102],
                            "reason": "필수 비타민과 미네랄을 공급하는 종합비타민에 눈 건강 기능성을 더하고, 에너지 생성을 돕는 마그네슘을 추가하여 전반적인 컨디션을 끌어올립니다."
                          }
                        ]
                      }
                    }
                    """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "잘못된 요청 (예: 목적을 4개 이상 선택)",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = CustomResponse.class),
                            examples = @ExampleObject(
                                    name = "400 에러 응답",
                                    value = """
                    {
                      "isSuccess": false,
                      "code": "CE0001",
                      "message": "요청이 올바르지 않습니다.",
                      "result": null
                    }
                    """
                            )
                    )
            )
    })
    public CustomResponse<AiRecommendationResponseDto> getAiCombinationRecommendations(
            @RequestBody AiRecommendationRequestDto requestDto
    ) throws IOException {
        AiRecommendationResponseDto response = recommendationService.getRecommendations(requestDto.getPurposes());
        return CustomResponse.ok(response);
    }
}