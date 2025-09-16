package com.vitacheck.domain.searchLog;

import com.vitacheck.common.entity.BaseTimeEntity;
import com.vitacheck.common.enums.Gender;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "search_logs")
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class SearchLog extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "user_id")
//    private User user;

    @Column(name = "user_id")
    private Long userId;

    @Column(nullable = false, length = 100)
    private String keyword;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Gender gender;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SearchCategory category;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Method method;

    @Column(name = "age")
    private Integer age;
}
