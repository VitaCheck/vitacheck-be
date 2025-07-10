package com.vitacheck.service;

import com.vitacheck.domain.Combination.Combination;
import com.vitacheck.domain.Combination.RecommandType;
import com.vitacheck.dto.CombinationDTO;
import com.vitacheck.repository.CombinationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CombinationService {

    private final CombinationRepository combinationRepository; // 리포지토리 주입


    public CombinationDTO.AnalysisResponse analyze(CombinationDTO.AnalysisRequest request) {
        // 1. 요청받은 ID로 DB에서 영양제 정보와 포함된 성분 정보들을 모두 조회합니다.
        // List<Supplement> supplements = supplementRepository.findAllById(request.getSupplementIds());

        // =================================================================
        //
        //        여기에 실제 분석 로직이 들어갑니다.
        //
        // 1. 조회된 영양제들의 성분들을 모두 합산하여 성분별 총 섭취량을 계산합니다.
        //    (예: A영양제 비타민C 500mg, B영양제 비타민C 500mg -> 총 1000mg)
        //
        // 2. 계산된 총 섭취량을 각 성분의 권장량/상한량과 비교합니다.
        //
        // 3. 포함된 성분들을 바탕으로 DB에 저장된 '궁합 정보(CombinationTips)'와 매칭하여
        //    주의가 필요한 조합과 궁합이 좋은 조합을 찾아냅니다.
        //
        // =================================================================

        // (임시) 분석 로직이 완성되었다고 가정하고, 더미 데이터로 응답 객체를 생성합니다.
        return buildDummyResponse();
    }

    // 로직 개발 전 임시로 사용할 더미 응답 데이터 생성 메소드
    private CombinationDTO.AnalysisResponse buildDummyResponse() {
        return CombinationDTO.AnalysisResponse.builder()
                .ingredientResults(List.of(
                        CombinationDTO.AnalysisResponse.IngredientAnalysisResultDto.builder()
                                .ingredientName("비타민 C")
                                .totalAmount(1000).unit("mg")
                                .recommendedAmount(100).upperAmount(2000)
                                .build()
                ))
                .build();
    }

    @Transactional(readOnly = true)
    public CombinationDTO.RecommendCombinationResponse getRecommendCombinations() {
        List<Combination> allCombinations = combinationRepository.findAll();

        List<CombinationDTO.RecommendResultDTO> goodList = allCombinations.stream()
                .filter(c -> c.getType() == RecommandType.GOOD)
                .map(CombinationDTO.RecommendResultDTO::from)
                .collect(Collectors.toList());

        List<CombinationDTO.RecommendResultDTO> cautionList = allCombinations.stream()
                .filter(c -> c.getType() == RecommandType.CAUTION)
                .map(CombinationDTO.RecommendResultDTO::from)
                .collect(Collectors.toList());

        return new CombinationDTO.RecommendCombinationResponse(goodList, cautionList);
    }
}
