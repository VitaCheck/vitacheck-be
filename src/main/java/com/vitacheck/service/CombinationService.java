package com.vitacheck.service;

import com.vitacheck.domain.Combination.Combination;
import com.vitacheck.domain.Combination.RecommandType;
import com.vitacheck.domain.Ingredient;
import com.vitacheck.domain.Supplement;
import com.vitacheck.domain.SupplementIngredient;
import com.vitacheck.dto.CombinationDTO;
import com.vitacheck.repository.CombinationRepository;
import com.vitacheck.repository.SupplementRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CombinationService {

    private final CombinationRepository combinationRepository; // 리포지토리 주입
    private final SupplementRepository supplementRepository;

    @Transactional(readOnly = true)
    public CombinationDTO.AnalysisResponse analyze(CombinationDTO.AnalysisRequest request) {
        // 1. 요청받은 ID로 DB에서 영양제와 성분 정보들을 한 번에 조회
        List<Supplement> supplements = supplementRepository.findSupplementsWithIngredientsByIds(request.getSupplementIds());

        // 2. 성분별 총 섭취량을 계산 (Key: 성분 Entity, Value: 총 함량)
        Map<Ingredient, Integer> totalAmountMap = new HashMap<>();
        for (Supplement supplement : supplements) {
            for (SupplementIngredient si : supplement.getSupplementIngredients()) {
                // 맵에 이미 성분이 있으면 기존 값에 더하고, 없으면 새로 추가
                totalAmountMap.merge(si.getIngredient(), si.getAmount(), Integer::sum);
            }
        }

        // 3. 계산된 결과를 최종 응답 DTO 리스트로 변환 
        List<CombinationDTO.AnalysisResponse.IngredientAnalysisResultDto> ingredientResults =
                totalAmountMap.entrySet().stream()
                        .map(entry -> {
                            Ingredient ingredient = entry.getKey();
                            int totalAmount = entry.getValue();
                            Integer recommendedAmount = ingredient.getRecommendedDosage();
                            Integer upperAmount = ingredient.getUpperLimit();

                            boolean isOver = (recommendedAmount != null && totalAmount >= recommendedAmount);

                            double ratio = 0.0;
                            // 권장량, 상한량이 모두 있고, 유효한 값일 때만 계산
                            if (recommendedAmount != null && upperAmount != null && recommendedAmount > 0) {
                                if (!isOver) {
                                    // 권장량 미만일 때
                                    ratio = (double) totalAmount / recommendedAmount;
                                } else {
                                    // 권장량 이상일 때
                                    double denominator = upperAmount - recommendedAmount;
                                    if (denominator > 0) {
                                        ratio = (double) (totalAmount - recommendedAmount) / denominator;
                                    }
                                }
                            }

                            return CombinationDTO.AnalysisResponse.IngredientAnalysisResultDto.builder()
                                    .ingredientName(ingredient.getName())
                                    .totalAmount(totalAmount)
                                    .unit(ingredient.getUnit())
                                    .recommendedAmount(recommendedAmount)
                                    .upperAmount(upperAmount)
                                    .isOverRecommended(isOver)
                                    .dosageRatio(Math.min(ratio, 2.0))
                                    .build();
                        })
                        .collect(Collectors.toList());

        return new CombinationDTO.AnalysisResponse(ingredientResults);
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
