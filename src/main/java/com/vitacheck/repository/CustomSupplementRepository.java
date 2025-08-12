package com.vitacheck.repository;

import com.vitacheck.domain.CustomSupplement;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CustomSupplementRepository extends JpaRepository<CustomSupplement, Long> {
    Optional<CustomSupplement> findByUserIdAndName(Long userId, String name);
}