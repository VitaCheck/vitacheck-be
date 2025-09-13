package com.vitacheck.product.domain.Purpose;

import com.vitacheck.product.domain.Ingredient.Ingredient;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "purpose_ingredients")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class PurposeIngredient {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 목적 (N:1)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "purpose_id")
    private Purpose purpose;

    // 성분 (N:1)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ingredient_id")
    private Ingredient ingredient;
}

