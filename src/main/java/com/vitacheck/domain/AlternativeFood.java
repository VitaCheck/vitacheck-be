package com.vitacheck.domain;

import com.vitacheck.common.entity.BaseTimeEntity;
import com.vitacheck.domain.mapping.IngredientAlternativeFood;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "alternative_foods")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class AlternativeFood extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "emoji")
    private String emoji;

    @OneToMany(mappedBy = "alternativeFood", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<IngredientAlternativeFood> ingredients = new ArrayList<>();

}
