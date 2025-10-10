package com.vitacheck.user.repository;

import com.vitacheck.user.domain.user.User;
import com.vitacheck.user.domain.user.UserStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);

    List<User> findByStatusAndUpdatedAtBefore(UserStatus status, LocalDateTime cutoffDate);
}