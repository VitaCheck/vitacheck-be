package com.vitacheck.product.controller;

import com.vitacheck.common.enums.Gender;
import com.vitacheck.common.CustomResponse;
import com.vitacheck.product.dto.SupplementResponseDTO;
import com.vitacheck.product.service.SupplementService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/supplements")
@Slf4j

public class SupplementController {

    private final SupplementService supplementService;


    @GetMapping("/search")
    @Operation(
            summary = "영양제 제품 검색 API (Cursor 기반 페이지네이션) By 박지영",
            description = """
                    키워드와 연관된 모든 영양제 제품이 조회됩니다. (성분, 브랜드, 제품 이름 등)

                    ex. 유산균 → 제품 이름에 유산균 포함된 영양제, 유산균을 성분으로 포함하는 영양제 등 모두 조회됩니다.

                    검색 키워드로 제품을 cursor 기반 페이지네이션으로 조회, 정렬은 인기도 높은 순으로(인기도=클릭수+검색수)

                    cursor 처음은 빈칸인채로 호출하면 됩니다. 그 이후 부터는 nextCursor 값이 아닌 '마지막으로 조회된 cursorID'를 넣어주면 됩니다.

                    nextcursor가 null이면 다음 페이지가 없다는 뜻입니다."""
    )
    public CustomResponse<SupplementResponseDTO.KeywordSearchSupplementBasedCursor> searchSupplements(
            @Parameter(name = "keyword", description = "검색 키워드", example = "유산균")
            @RequestParam String keyword,
            @Parameter(name = "cursorId", description = "마지막 조회된 아이디 (인기도 *1000000 + supplementId)", example = "")
            @RequestParam(required = false) Long cursorId,
            @Parameter(name = "size", description = "가져올 데이터 개수", example = "40")
            @RequestParam(defaultValue = "40") int size
    ) {
        SupplementResponseDTO.KeywordSearchSupplementBasedCursor responseDto =
                supplementService.searchSupplements(keyword, cursorId,size);

        return CustomResponse.ok(responseDto);
    }




    // 특정 영양제 상세 정보 반환 API
    @GetMapping
    @Operation(summary = "영양제 상세 조회", description = "supplementId로 상세 정보를 조회합니다. ❗변경사항 상세조회에서 좋아요 조회하는거 따로 분리함")
    public CustomResponse<SupplementResponseDTO.SupplementDetail> getSupplement(
            @RequestParam Long id) {
        SupplementResponseDTO.SupplementDetail responseDto=supplementService.getSupplementDetail(id);
        return CustomResponse.ok(responseDto);
    }




    // 특정 브랜드 다른 영양제 목록 반환 API
    @GetMapping("/brand")
    @Operation(summary = "특정 브랜드의 다른 영양제 목록 반환", description = "특정 브랜드의 다른 영양제 목록을 반환합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공")
    })
    public Map<String, List<SupplementResponseDTO.SimpleResponse>> getByBrandId(@RequestParam Long id) {
        List<SupplementResponseDTO.SimpleResponse> list = supplementService.getSupplementsByBrandId(id);
        return Map.of("supplements", list);
    }

    // 특정 영양제의 상세정보 반환 API DTO
    @GetMapping("/detail")
    @Operation(summary = "영양제 상세 조회", description = "성분별 함량, 상태, 시각화 정보 등을 반환합니다.")
    public SupplementResponseDTO.DetailResponse getSupplementDetail(@RequestParam Long id) {
        return supplementService.getSupplementDetailById(id);
    }

}
