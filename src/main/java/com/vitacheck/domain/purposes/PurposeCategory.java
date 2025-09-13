package com.vitacheck.domain.purposes;

import com.vitacheck.domain.Ingredient;
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
public class PurposeCategory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Enum으로 저장됨 -> 목적 이름
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, unique = true)
    private AllPurpose name;

    @OneToMany(mappedBy = "purposeCategory", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<PurposeIngredient> purposeIngredients = new ArrayList<>();

}
