package com.vitacheck.domain.purposes;

import com.vitacheck.domain.Ingredient;
import com.vitacheck.domain.common.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

// AllPurpose의 DB 엔티티화
@Entity
@Table(name = "purpose_categories")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class PurposeCategory extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Enum으로 저장됨 -> 목적 이름
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, unique = true)
    private AllPurpose name;

    private String imageUrl;

    private Integer displayOrder; // 정렬 순서

    @ManyToMany
    @JoinTable(
            name = "purpose_ingredient", // 생성될 매핑 테이블의 이름
            joinColumns = @JoinColumn(name = "purpose_category_id"),
            inverseJoinColumns = @JoinColumn(name = "ingredient_id")
    )
    @Builder.Default
    private List<Ingredient> ingredients = new ArrayList<>();
}
