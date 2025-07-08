package com.vitacheck.service;

import com.vitacheck.dto.FoodSafetyApiResponseDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class FoodSafetyApiService {

    private final RestTemplate restTemplate;

    @Value("${open-api.food-safety-korea.api-key}")
    private String apiKey;

    private final String API_BASE_URL = "http://openapi.foodsafetykorea.go.kr/api";
    private final String SERVICE_ID = "C003";
    private final String DATA_TYPE = "json";

    /**
     * 원료인정번호로 영양제 정보를 검색합니다.
     * @param recognitionNo 검색할 원료인정번호
     * @return 검색 결과 리스트
     */
    public List<FoodSafetyApiResponseDto.SupplementInfo> searchSupplementsByRecognitionNo(String recognitionNo) {
        log.info("식품안전나라 API 호출 시작: 원료인정번호 = {}", recognitionNo);
        return callApi("HF_FNCLTY_MTRAL_RCOGN_NO", recognitionNo);
    }

    /**
     * 원재료명으로 영양제 정보를 검색합니다.
     * @param rawMaterialName 검색할 원재료명
     * @return 검색 결과 리스트
     */
    public List<FoodSafetyApiResponseDto.SupplementInfo> searchSupplementsByRawMaterial(String rawMaterialName) {
        log.info("식품안전나라 API 호출 시작: 원재료명 = {}", rawMaterialName);
        return callApi("RAWMTRL_NM", rawMaterialName);
    }

    /**
     * 식품안전나라 API를 호출하는 공통 메소드
     * @param searchField API 요청 시 사용할 검색 필드명 (예: "PRDLST_NM")
     * @param searchValue 검색할 값
     * @return API 응답 결과 리스트
     */
    private List<FoodSafetyApiResponseDto.SupplementInfo> callApi(String searchField, String searchValue) {
        int startIndex = 1;
        int endIndex = 10; // 우선 10개만 가져오도록 설정

        URI uri = UriComponentsBuilder
                .fromUriString(API_BASE_URL)
                .pathSegment(apiKey, SERVICE_ID, DATA_TYPE, String.valueOf(startIndex), String.valueOf(endIndex))
                .queryParam(searchField, searchValue)
                .build()
                .toUri();

        try {
            FoodSafetyApiResponseDto response = restTemplate.getForObject(uri, FoodSafetyApiResponseDto.class);
            if (response != null && response.getC003() != null && response.getC003().getSupplements() != null) {
                log.info("API 호출 성공: {}개의 결과를 찾았습니다.", response.getC003().getTotalCount());
                return response.getC003().getSupplements();
            } else {
                log.warn("API 호출은 성공했으나, 유효한 데이터가 없습니다. 응답: {}", response);
                return Collections.emptyList();
            }
        } catch (Exception e) {
            log.error("식품안전나라 API 호출 중 에러 발생: {}", e.getMessage());
            throw new RuntimeException("식품안전나라 API 호출에 실패했습니다.");
        }
    }
}
