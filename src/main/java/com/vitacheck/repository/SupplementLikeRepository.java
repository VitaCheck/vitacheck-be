package com.vitacheck.repository;

import com.vitacheck.domain.SupplementLike;
import com.vitacheck.domain.Supplement;
import com.vitacheck.domain.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.List;

public interface SupplementLikeRepository extends JpaRepository<SupplementLike, Long> {

    // 사용자가 이미 특정 영양제를 찜했는지 확인
    boolean existsByUserAndSupplement(User user, Supplement supplement);

    Optional<SupplementLike> findByUserAndSupplement(User user, Supplement supplement);

    @Query("SELECT l FROM SupplementLike l JOIN FETCH l.supplement s JOIN FETCH s.brand WHERE l.user.id = :userId")
    List<SupplementLike> findAllByUserIdWithSupplement(@Param("userId") Long userId);

    boolean existsByUserIdAndSupplementId(Long userId, Long supplementId);

    void deleteAllByUser(User user);
}
