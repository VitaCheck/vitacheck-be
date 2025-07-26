package com.vitacheck.service;

import com.vitacheck.domain.AgeGroupIngredientStats;
import com.vitacheck.domain.AgeGroupSupplementStats;
import com.vitacheck.domain.Ingredient;
import com.vitacheck.domain.user.User;
import com.vitacheck.repository.AgeGroupIngredientStatsRepository;
import com.vitacheck.repository.AgeGroupSupplementStatsRepository;
import com.vitacheck.repository.IngredientRepository;
import com.vitacheck.repository.SupplementRepository;
import com.vitacheck.util.DateUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class StatisticsService {

    private final AgeGroupSupplementStatsRepository ageGroupSupplementStatsRepository;
    private final SupplementRepository supplementRepository;
    private final AgeGroupIngredientStatsRepository ageGroupIngredientStatsRepository;
    private final IngredientRepository ingredientRepository;

    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void incrementSupplementClickCount(User user, Long supplementId) {
        supplementRepository.findById(supplementId).ifPresent(supplement -> {
            String ageGroup = DateUtils.calculateAgeGroup(user.getBirthDate());

            AgeGroupSupplementStats stats = ageGroupSupplementStatsRepository
                    .findBySupplementAndAge(supplement, ageGroup)
                    .orElseGet(() -> AgeGroupSupplementStats.builder()
                            .supplement(supplement)
                            .age(ageGroup)
                            .clickCount(0L)
                            .build());

            stats.incrementClickCount();
            ageGroupSupplementStatsRepository.save(stats);
        });
    }

    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void updateIngredientSearchStats(User user, String ingredientName) {
        log.info(">>>>> 성분 통계 업데이트 시작: userId={}, ingredientName={}", user.getId(), ingredientName);

        String ageGroup = DateUtils.calculateAgeGroup(user.getBirthDate());
        log.info(">>>>> 계산된 연령대: {}", ageGroup);

        // 성분 이름으로 성분 엔티티 조회
        Optional<Ingredient> optIngredient = ingredientRepository.findByName(ingredientName);

        if (optIngredient.isPresent()) {
            Ingredient ingredient = optIngredient.get();
            log.info(">>>>> DB에서 성분을 찾았습니다: {}", ingredient.getName());

            AgeGroupIngredientStats stats = ageGroupIngredientStatsRepository
                    .findByIngredientAndAge(ingredient, ageGroup)
                    .orElseGet(() -> {
                        log.info(">>>>> 새로운 통계 데이터를 생성합니다.");
                        return AgeGroupIngredientStats.builder()
                                .ingredient(ingredient)
                                .age(ageGroup)
                                .searchCount(0)
                                .build();
                    });

            stats.incrementSearchCount();
            ageGroupIngredientStatsRepository.save(stats);
            log.info(">>>>> 통계 저장 완료!");

        } else {
            log.warn(">>>>> DB에서 '{}' 성분을 찾을 수 없습니다. 통계를 업데이트하지 않습니다.", ingredientName);
        }
    }

}