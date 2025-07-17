package com.vitacheck.repository;

import com.vitacheck.domain.combination.Combination;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CombinationRepository extends JpaRepository<Combination, Long> {
}
