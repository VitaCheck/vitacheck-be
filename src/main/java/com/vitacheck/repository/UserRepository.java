package com.vitacheck.repository;

import com.vitacheck.domain.user.User;
import com.vitacheck.domain.user.UserStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);

    // 카카오 로그인 구현에서 이메일 제공 불가(애플리케이션 심사 필요) -> 가상 이메일 생성 메소드
    Optional<User> findByProviderAndProviderId(String provider, String providerId);

    List<User> findByStatusAndUpdatedAtBefore(UserStatus status, LocalDateTime cutoffDate);
}
