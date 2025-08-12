package com.vitacheck.domain;

import com.vitacheck.domain.common.BaseTimeEntity;
import com.vitacheck.domain.user.User;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "custom_supplements",
        uniqueConstraints = @UniqueConstraint(columnNames = {"user_id","name"}))
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class CustomSupplement extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(name = "image_url", length = 500)
    private String imageUrl;

    public void update(String name, String imageUrl) {
        this.name = name;
        this.imageUrl = imageUrl;
    }
}
