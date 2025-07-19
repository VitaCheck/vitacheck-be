package com.vitacheck.domain;

import com.vitacheck.domain.common.BaseTimeEntity;
import com.vitacheck.domain.mapping.SupplementIngredient;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "supplements")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Supplement extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "brand_id", nullable = false)
    private Brand brand;

    @Column(name = "name", nullable = false, length = 50)
    private String name;

    @Column(name = "coupang_url", length = 255)
    private String coupangUrl;

    @Column(name = "image_url", length = 255)
    private String imageUrl;

    @Column(name = "price")
    private Integer price;

    @Column(name = "description", length = 255)
    private String description;

    private String method;

    private String caution;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    // 영양제(1) <-> 영양제-성분(N)
    @Builder.Default
    @OneToMany(mappedBy = "supplement", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<SupplementIngredient> supplementIngredients = new ArrayList<>();
}
