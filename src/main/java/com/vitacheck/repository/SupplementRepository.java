package com.vitacheck.repository;

import com.vitacheck.domain.Supplement;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SupplementRepository extends JpaRepository<Supplement, Long>, SupplementRepositoryCustom {
}