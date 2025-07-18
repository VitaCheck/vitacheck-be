package com.vitacheck.repository;

import com.vitacheck.domain.notification.NotificationRoutine;
import com.vitacheck.domain.RoutineDayOfWeek;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

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

    List<NotificationRoutine> findAllByUserId(Long userId);

    @Query("""
        SELECT r FROM NotificationRoutine r
        JOIN FETCH r.user
        JOIN FETCH r.supplement
        WHERE r.isEnabled = true
          AND r.id IN (SELECT rd.notificationRoutine.id FROM RoutineDay rd WHERE rd.dayOfWeek = :dayOfWeek)
          AND r.id IN (SELECT rt.notificationRoutine.id FROM RoutineTime rt WHERE rt.time = :time)
    """)
    List<NotificationRoutine> findRoutinesToSend(RoutineDayOfWeek dayOfWeek, LocalTime time);

    // 사용자와 루틴 ID 모두 일치하는 루틴만 조회
    Optional<NotificationRoutine> findByIdAndUserId(Long id, Long userId);
}