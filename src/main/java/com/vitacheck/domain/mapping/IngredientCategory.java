package com.vitacheck.domain.mapping;

import com.vitacheck.domain.Ingredient;
import com.vitacheck.domain.purposes.PurposeCategory;
import jakarta.persistence.*;
import lombok.*;

// 목적(카테고리) - 영양성분 매핑테이블
@Entity
@Table(name = "ingredient_categories")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class IngredientCategory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ingredient_id", nullable = false)
    private Ingredient ingredient;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private PurposeCategory category;
}

