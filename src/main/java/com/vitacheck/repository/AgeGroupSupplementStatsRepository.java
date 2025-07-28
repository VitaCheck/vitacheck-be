package com.vitacheck.repository; // 패키지 경로는 실제 프로젝트에 맞게 조정하세요.

import com.vitacheck.domain.Supplement;
import com.vitacheck.domain.AgeGroupSupplementStats;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AgeGroupSupplementStatsRepository extends JpaRepository<AgeGroupSupplementStats, Long> {

    /**
     * 영양제와 연령대를 기준으로 통계 데이터를 조회합니다.
     * @param supplement 조회할 영양제 엔티티
     * @param age 조회할 연령대 문자열 (예: "20대")
     * @return 통계 데이터가 있으면 Optional<AgeGroupSupplementStats>, 없으면 Optional.empty()
     */
    Optional<AgeGroupSupplementStats> findBySupplementAndAge(Supplement supplement, String age);
}