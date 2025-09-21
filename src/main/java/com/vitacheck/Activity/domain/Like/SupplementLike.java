package com.vitacheck.Activity.domain.Like;


import com.vitacheck.common.entity.BaseTimeEntity;

import com.vitacheck.product.domain.Supplement.Supplement;
import com.vitacheck.user.domain.User;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "supplement_likes", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"user_id", "supplement_id"})
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class SupplementLike extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "supplement_id", nullable = false)
    private Supplement supplement;
}
