package com.vitacheck.domain;

import com.vitacheck.domain.common.BaseTimeEntity;
import com.vitacheck.domain.mapping.IngredientAlternativeFood;
import com.vitacheck.domain.mapping.SupplementIngredient;
import com.vitacheck.domain.purposes.PurposeIngredient;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.util.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "ingredients")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class Ingredient extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "description",columnDefinition = "TEXT")
    private String description;

    @Column(name = "caution",columnDefinition = "TEXT")
    private String caution;

    @Column(name = "effect",columnDefinition = "TEXT")
    private String effect;

    @Column(name = "unit", length = 20)
    private String unit;

    @OneToMany(mappedBy = "ingredient")
    private List<IngredientDosage> dosages = new ArrayList<>();

    @OneToMany(mappedBy = "ingredient", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<SupplementIngredient> supplementIngredients = new HashSet<>();

    @OneToMany(mappedBy = "ingredient", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<IngredientAlternativeFood> alternativeFoods = new ArrayList<>();

//    @ManyToMany(mappedBy = "ingredients")
//    private List<PurposeCategory> purposeCategories = new ArrayList<>();
// 🔹 중간 테이블(PurposeIngredient)과 연결
    @OneToMany(mappedBy = "ingredient", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PurposeIngredient> purposeIngredients = new ArrayList<>();
}