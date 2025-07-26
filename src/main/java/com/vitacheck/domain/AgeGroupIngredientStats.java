package com.vitacheck.domain;

import com.vitacheck.domain.Ingredient;
import com.vitacheck.domain.common.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "age_group_ingredient_stats")
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class AgeGroupIngredientStats extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 통계(N) -> 성분(1)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ingredient_id")
    private Ingredient ingredient;

    @Column(nullable = false, length = 10)
    private String age;

    @Column(nullable = false)
    private Integer searchCount;

    /**
     * 검색 카운트를 1 증가시키는 헬퍼 메소드
     */
    public void incrementSearchCount() {
        this.searchCount++;
    }
}