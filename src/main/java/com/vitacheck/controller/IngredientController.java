package com.vitacheck.controller;

import com.vitacheck.domain.Ingredient;
import com.vitacheck.dto.IngredientResponseDTO;
import com.vitacheck.global.apiPayload.CustomResponse;
import com.vitacheck.repository.IngredientRepository;
import com.vitacheck.service.IngredientService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequiredArgsConstructor
public class IngredientController {
    private final IngredientService ingredientService;
    private final IngredientRepository ingredientRepository;


    @Operation(
            summary = "성분 검색 조회 API By 박지영",
            description = """
        성분의 이름으로 검색 결과를 반환합니다...
        해당 성분을 사용자가 누르면, 성분 id로 성분 상세 조회를 요청해주세요
        """

    )
    @GetMapping("/api/v1/ingredients/search")
    public CustomResponse<List<IngredientResponseDTO.IngredientName>> getIngredientFood(
            @Parameter(name = "keyword", description = "검색 키워드", example = "유산균")
            @RequestParam String keyword) {
        List<IngredientResponseDTO.IngredientName> responseDto = ingredientService.searchIngredientName(keyword);
        return CustomResponse.ok(responseDto);
    }

    @Operation(
            summary = "성분 상세 조회 API By 박지영",
            description = """
        성분의 설명, 효능, 부작용 및 주의사항, 상한 섭취량, 하한 섭취량, 대체식품 등을 반환합니다..
        
        dosageErrorCode, foodErrorCode, supplementErrorCode 는 아래의 에러들을 나타냅니다. 에러에 따라서 화면 처리 부탁드립니다.
        
        1. dosageErrorCode
        
        **`UNAUTHORIZED`** : 미로그인 사용자 (상한 , 권장량, 나이, 성별 정보 없음)
        
        **`INGREDIENT_DOSAGE_NOT_FOUND` :** 해당 성분의 상한과 권장량 데이터 아예 없음 (하한과 상한 -1로 처리함)
        
        **`INGREDIENT_DOSAGE_HAVE_NULL` :** 해당 성분의 상한과 권장량 중에서 하나의 값이 없음 (없는 값은 null로 처리함)
        
        2. foodErrorCode
        
        **`INGREDIENT_FOOD_NOT_FOUND` :** 해당 성분의 대체 식품 없음
        
        3. supplementErrorCode
        
        **`INGREDIENT_SUPPLEMENT_NOT_FOUND` :** 해당 성분 관련 영양제 없음
        
        
        """

    )
    @GetMapping("/api/v1/ingredients/{id}")
    public CustomResponse<IngredientResponseDTO.IngredientDetails> getIngredientDetails(
            @Parameter(name = "id", description = "성분 ID", example = "1")
            @PathVariable Long id) {
        IngredientResponseDTO.IngredientDetails responseDto = ingredientService.getIngredientDetails(id);
        return CustomResponse.ok(responseDto);
    }



}
