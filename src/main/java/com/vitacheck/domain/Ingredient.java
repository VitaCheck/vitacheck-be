package com.vitacheck.domain;

import com.vitacheck.domain.mapping.SupplementIngredient;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "ingredients")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class Ingredient {

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

    @Column(name = "lower_limit")
    private Double lowerLimit;

    @Column(name = "recommended_")
    private Double recommendedDosage;

    @Column(name = "upper_limit")
    private Double upperLimit;

    @Column(name = "unit", length = 20)
    private String unit;

    @Column(name = "created_at")
    @CreatedDate
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    @LastModifiedDate
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "ingredient", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<SupplementIngredient> supplementIngredients = new ArrayList<>();
}