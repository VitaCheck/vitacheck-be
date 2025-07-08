package com.vitacheck.controller;

import com.vitacheck.dto.FoodSafetyApiResponseDto;
import com.vitacheck.service.FoodSafetyApiService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class TestController {

    private final FoodSafetyApiService foodSafetyApiService;

    @Operation(summary = "원료인정번호로 영양제 검색", description = "식품안전나라 API를 이용해 원료인정번호로 영양제 정보를 검색합니다.")
    @GetMapping("/search-by-recognition-no")
    public ResponseEntity<List<FoodSafetyApiResponseDto.SupplementInfo>> searchByRecognitionNo(
            @Parameter(description = "검색할 원료인정번호", required = true, example = "제2012-36호") // 예시 값 수정
            @RequestParam("recognitionNo") String recognitionNo
    ) {
        List<FoodSafetyApiResponseDto.SupplementInfo> results = foodSafetyApiService.searchSupplementsByRecognitionNo(recognitionNo);
        return ResponseEntity.ok(results);
    }

    @Operation(summary = "원재료명으로 영양제 검색", description = "식품안전나라 API를 이용해 원재료명으로 영양제 정보를 검색합니다.")
    @GetMapping("/search-by-raw-material")
    public ResponseEntity<List<FoodSafetyApiResponseDto.SupplementInfo>> searchByRawMaterial(
            @Parameter(description = "검색할 원재료명", required = true, example = "핑거루트추출분말") // 예시 값 수정
            @RequestParam("rawMaterial") String rawMaterial
    ) {
        List<FoodSafetyApiResponseDto.SupplementInfo> results = foodSafetyApiService.searchSupplementsByRawMaterial(rawMaterial);
        return ResponseEntity.ok(results);
    }
}
