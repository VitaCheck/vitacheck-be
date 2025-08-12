package com.vitacheck.domain.mapping;

import com.vitacheck.domain.Ingredient;
import com.vitacheck.domain.Supplement;
import com.vitacheck.domain.common.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "supplement_ingredients")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SupplementIngredient extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 영양제-성분(N) -> 영양제(1)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "supplement_id")
    private Supplement supplement;

    // 영양제-성분(N) -> 성분(1)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ingredient_id")
    private Ingredient ingredient;

    private Double amount;
}