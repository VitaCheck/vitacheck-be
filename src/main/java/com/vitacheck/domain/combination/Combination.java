package com.vitacheck.domain.combination;

import com.vitacheck.common.entity.BaseTimeEntity;
import com.vitacheck.domain.Ingredient;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class Combination extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    private RecommandType type;

    private String name;

    private String description;

    private Integer displayRank;

    @ManyToMany
    @JoinTable(
            name = "combination_ingredient", // 생성될 매핑 테이블의 이름
            joinColumns = @JoinColumn(name = "combination_id"),
            inverseJoinColumns = @JoinColumn(name = "ingredient_id")
    )
    @Builder.Default
    private List<Ingredient> ingredients = new ArrayList<>();
}
