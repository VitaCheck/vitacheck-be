package com.vitacheck.controller;

import com.vitacheck.domain.Ingredient;
import com.vitacheck.dto.IngredientResponseDTO;
import com.vitacheck.global.apiPayload.CustomResponse;
import com.vitacheck.service.IngredientService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;


@RestController
@RequiredArgsConstructor
public class IngredientController {
    private final IngredientService ingredientService;

    @Operation(
            summary = "성분 상세 조회 API By 박지영",
            description = """
        성분의 설명, 효능, 부작용 및 주의사항, 상한 섭취량, 하한 섭취량 등을 반환합니다..
        """

    )
    @GetMapping("/api/ingredients/{id}")
    public CustomResponse<IngredientResponseDTO.IngredientDetails> getIngredientDetails(@PathVariable Long id) {
        IngredientResponseDTO.IngredientDetails responseDto = ingredientService.getIngredientDetails(id);
        return CustomResponse.ok(responseDto);
    }

}
