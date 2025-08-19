package com.vitacheck.domain.purposes;

import com.vitacheck.domain.Ingredient;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "purpose_ingredient")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class PurposeIngredient {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 목적
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "purpose_id")
    private PurposeCategory purposeCategory;

    // 성분
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ingredient_id")
    private Ingredient ingredient;
}
