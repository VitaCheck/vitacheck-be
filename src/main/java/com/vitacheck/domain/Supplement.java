package com.vitacheck.domain;

import jakarta.persistence.*;
import lombok.*;

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
}
