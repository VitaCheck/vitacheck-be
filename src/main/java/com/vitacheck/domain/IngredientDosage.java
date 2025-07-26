package com.vitacheck.domain;

import com.vitacheck.domain.user.Gender;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "ingredient_dosages")
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class IngredientDosage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ingredient_id")
    private Ingredient ingredient;

    @Enumerated(EnumType.STRING)
    private Gender gender; // MALE, FEMALE, ALL

    private Integer minAge;
    private Integer maxAge;
    private Double recommendedDosage;
    private Double upperLimit;

    @Column(nullable = false, length = 20)
    private String unit;
}