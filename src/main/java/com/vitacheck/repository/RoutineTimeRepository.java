package com.vitacheck.repository;

import com.vitacheck.domain.RoutineTime;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RoutineTimeRepository extends JpaRepository<RoutineTime, Long> {
}