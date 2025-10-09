package com.vitacheck.product.domain.Purpose;


import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "purposes")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Purpose {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 목적 이름 (예: 눈 건강, 뼈 건강 등)
    @Column(nullable = false, unique = true, length = 50)
    private String name;

    // 목적 enum (예: EYE, BONE)
    @Enumerated(EnumType.STRING)
    @Column(name = "enum_code", nullable = false, unique = true)
    private AllPurpose enumCode;

    // Purpose가 관리하는 성분들
    @OneToMany(mappedBy = "purpose", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<PurposeIngredient> purposeIngredients = new ArrayList<>();
}


