package com.vitacheck.controller;

import com.vitacheck.domain.Ingredient;
import com.vitacheck.dto.IngredientResponseDTO;
import com.vitacheck.dto.PopularIngredientDto;
import com.vitacheck.global.apiPayload.CustomResponse;
import com.vitacheck.repository.IngredientRepository;
import com.vitacheck.service.IngredientService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequiredArgsConstructor
public class IngredientController {
    private final IngredientService ingredientService;


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
       
        """

    )
    @GetMapping("/api/v1/ingredients/{id}")
    public CustomResponse<IngredientResponseDTO.IngredientDetails> getIngredientDetails(
            @Parameter(name = "id", description = "성분 ID", example = "1")
            @PathVariable Long id) {
        IngredientResponseDTO.IngredientDetails responseDto = ingredientService.getIngredientDetails(id);
        return CustomResponse.ok(responseDto);
    }

    @Operation(
            summary = "성분 관련 영양제 조회 (cursor 기반) API By 박지영",
            description = """
        성분 관련 영양제를 cursor기반으로 반환합니다..
        
        cursor 처음은 빈칸 또는 0으로 호출하면 됩니다. 그 이후 부터는 nextCursor 값이 아닌 '마지막으로 조회된 ID'를 넣어주면 됩니다.
                    
        nextcursor가 null이면 다음 페이지가 없다는 뜻입니다.
        """

    )
    @GetMapping("/api/v1/ingredients/{id}/supplements")
    public CustomResponse<IngredientResponseDTO.IngredientSupplementBasedCursor> getIngredientSupplementBasedCursor(
            @Parameter(name = "id", description = "성분 ID", example = "1")
            @PathVariable Long id,
            @Parameter(name = "cursor", description = "이전 페이지 마지막 supplement ID", example = "1")
            @RequestParam(required = false) Long cursor,
            @Parameter(name = "size", description = "가져올 데이터 개수", example = "40")
            @RequestParam(defaultValue = "40") int size) {
        IngredientResponseDTO.IngredientSupplementBasedCursor responseDto =
                ingredientService.getIngredientSupplementBasedCursor(id, cursor, size);
        return CustomResponse.ok(responseDto);
    }


    @Operation(summary = "인기 검색 성분 조회", description = "가장 많이 검색된 성분을 순서대로 조회합니다.")
    @GetMapping("/popular-ingredients")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "인기 검색 성분 조회",
                    content = @Content(examples = @ExampleObject(value = "{\"isSuccess\":true,\"code\":\"COMMON200\",\"message\":\"성공적으로 요청을 수행했습니다.\",\"result\":\"FCM 토큰이 업데이트되었습니다.\"}"))),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자",
                    content = @Content(examples = @ExampleObject(value = "{\"isSuccess\":false,\"code\":\"U0001\",\"message\":\"로그인이 필요합니다.\",\"result\":null}"))),
            @ApiResponse(responseCode = "404", description = "사용자를 찾을 수 없음",
                    content = @Content(examples = @ExampleObject(value = "{\"isSuccess\":false,\"code\":\"U0002\",\"message\":\"사용자를 찾을 수 없습니다.\",\"result\":null}")))
    })
    public CustomResponse<List<PopularIngredientDto>> getPopularIngredients(
            @Parameter(description = "조회할 연령대", required = true, example = "20대",
                    schema = @Schema(type = "string", allowableValues = {"10대", "20대", "30대", "40대", "50대", "60대 이상", "전체"}))
            @RequestParam String ageGroup,

            @Parameter(description = "상위 몇 개까지 조회할지", example = "10")
            @RequestParam(defaultValue = "10") int limit
    ) {
        List<PopularIngredientDto> result = ingredientService.findPopularIngredients(ageGroup, limit);
        return CustomResponse.ok(result);
    }
}
