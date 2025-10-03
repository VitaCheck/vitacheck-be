package com.vitacheck.product.controller;

import com.vitacheck.common.CustomResponse;
import com.vitacheck.product.domain.Purpose.AllPurpose;
import com.vitacheck.product.dto.PurposeResponseDTO;
import com.vitacheck.product.service.PurposeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/purposes")
@RequiredArgsConstructor
@Tag(name = "Purpose", description = "건강 목적 Enum API")
public class PurposeController {

    private final PurposeService purposeService;

    @GetMapping
    @Operation(
            summary = "건강 목적 전체 조회",
            description = "서버에 정의된 모든 건강 목적(enum) 리스트를 반환합니다.\n\n" +
                    "목적 드롭다운 버튼, 목적 전체 보여주기에 사용 가능합니다."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "조회 성공",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = PurposeResponseDTO.AllPurposeDTO.class)))
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "서버 내부 오류",
                    content = @Content
            )
    })
    public CustomResponse<List<PurposeResponseDTO.AllPurposeDTO>> getAllPurposes() {
        List<PurposeResponseDTO.AllPurposeDTO> purposes = purposeService.getAllPurposes();
        return CustomResponse.ok(purposes);
    }

    @GetMapping("/filter")
    @Operation(
            summary = "목적별 필터한 결과 조회 API By 박지영",
            description = """
        선택한 목적으로 성분과 관련 영양제를 결과를 반환합니다...
        - 목적은 쿼리 파라미터로 전달합니다.
        - 다수 선택 시 같은 파라미터를 반복해서 전달합니다.
          예: /api/v1/purpose?goals=eye&goals=bone&goals=stress
        """
    )
    public CustomResponse<List<PurposeResponseDTO.PurposeWithIngredientSupplement>> getPurposesWithIngredienSupplement(
            // goals = ["eye", "bone", "stress"]
            @Parameter(name = "goals", description = "검색 목적 리스트(다중 선택 가능)", example = "[\"eye\", \"bone\", \"stress\"]")
            @RequestParam("goals") List<String> goalsEnum   // ✅ DTO랑 안 겹침 goalsEnum
    ) {
        List<PurposeResponseDTO.PurposeWithIngredientSupplement> responseDto = purposeService.findByGoals(goalsEnum);
        return CustomResponse.ok(responseDto);
    }



}

