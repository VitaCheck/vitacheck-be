package com.vitacheck.repository;

import com.vitacheck.domain.RoutineDetail;
import com.vitacheck.domain.notification.NotificationRoutine;
import com.vitacheck.domain.RoutineDayOfWeek;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface NotificationRoutineRepository extends JpaRepository<NotificationRoutine, Long> {

    @Query("SELECT rd FROM RoutineDetail rd " +
            "JOIN rd.notificationRoutine nr " +
            "WHERE nr.user.id = :userId " +
            "AND nr.supplement.id = :supplementId")
    List<RoutineDetail> findRoutineDetailsByUserIdAndSupplementId(@Param("userId") Long userId, @Param("supplementId") Long supplementId);

    List<NotificationRoutine> findAllByUserId(Long userId);

    @Query("""
        SELECT r FROM NotificationRoutine r
        JOIN FETCH r.user
        JOIN FETCH r.supplement
        WHERE r.isEnabled = true
          AND r.id IN (
            SELECT rd.notificationRoutine.id FROM RoutineDetail rd
            WHERE rd.dayOfWeek = :dayOfWeek AND rd.time = :time
          )
    """)
    List<NotificationRoutine> findRoutinesToSend(@Param("dayOfWeek") RoutineDayOfWeek dayOfWeek, @Param("time") LocalTime time);

    // 사용자와 루틴 ID 모두 일치하는 루틴만 조회
    Optional<NotificationRoutine> findByIdAndUserId(Long id, Long userId);
}