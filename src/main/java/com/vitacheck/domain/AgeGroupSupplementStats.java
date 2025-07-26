package com.vitacheck.domain;


import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "age_group_supplement_stats")
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class AgeGroupSupplementStats {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "supplement_id")
    private Supplement supplement;

    @Column(nullable = false, length = 10)
    private String age;

    @Column(nullable = false)
    private Long clickCount;

    public void incrementClickCount() {
        this.clickCount++;
    }
}


