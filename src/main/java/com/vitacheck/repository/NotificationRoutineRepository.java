package com.vitacheck.repository;

import com.vitacheck.domain.NotificationRoutine;
import com.vitacheck.domain.RoutineDayOfWeek;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalTime;
import java.util.List;

@Repository
public interface NotificationRoutineRepository extends JpaRepository<NotificationRoutine, Long> {

    @Query("""
        SELECT COUNT(r) > 0
        FROM NotificationRoutine r
        JOIN r.routineDays d
        JOIN r.routineTimes t
        WHERE r.user.id = :userId
          AND r.supplement.id = :supplementId
          AND d.dayOfWeek IN :days
          AND t.time IN :times
    """)
    boolean existsDuplicateRoutine(
            Long userId,
            Long supplementId,
            List<RoutineDayOfWeek> days,
            List<LocalTime> times
    );
}