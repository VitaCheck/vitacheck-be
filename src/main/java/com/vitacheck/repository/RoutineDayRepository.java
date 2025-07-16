package com.vitacheck.repository;

import com.vitacheck.domain.RoutineDay;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RoutineDayRepository extends JpaRepository<RoutineDay, Long> {
}