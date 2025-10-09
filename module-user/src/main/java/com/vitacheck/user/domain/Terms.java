package com.vitacheck.user.domain;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;

@Entity
@Table(name = "terms")
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Terms {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title; // 예: "서비스 이용약관", "개인정보 처리방침"

    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;

    @Column(nullable = false)
    private String version; // 예: "v1.0"

    @Column(nullable = false)
    private boolean isRequired; // 필수 동의 여부

    private LocalDate effectiveDate; // 시행 일자
}