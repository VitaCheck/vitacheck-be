package com.vitacheck.Notification.repository;

import com.vitacheck.Intake.domain.RoutineDetail;
import com.vitacheck.Notification.domain.NotificationRoutine;
import com.vitacheck.Intake.domain.RoutineDayOfWeek;
import com.vitacheck.user.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface NotificationRoutineRepository extends JpaRepository<NotificationRoutine, Long> {

    @Query("""
        SELECT rd FROM RoutineDetail rd
        JOIN rd.notificationRoutine nr
        WHERE nr.user.id = :userId
          AND nr.supplement.id = :supplementId
    """)
    List<RoutineDetail> findRoutineDetailsByUserIdAndSupplementId(
            @Param("userId") Long userId,
            @Param("supplementId") Long supplementId);

    List<NotificationRoutine> findAllByUserId(Long userId);

    // 🔹 조회용: 사용자 루틴 전부 (user + catalog/custom까지 fetch)
    @Query("""
        SELECT r FROM NotificationRoutine r
        JOIN FETCH r.user
        LEFT JOIN FETCH r.supplement
        LEFT JOIN FETCH r.customSupplement
        WHERE r.user.id = :userId
    """)
    List<NotificationRoutine> findAllWithTargetsByUserId(@Param("userId") Long userId);

    // 🔹 알림 전송 대상: 카탈로그/커스텀 모두 포함
    @Query("""
        SELECT r FROM NotificationRoutine r
        JOIN FETCH r.user
        LEFT JOIN FETCH r.supplement
        LEFT JOIN FETCH r.customSupplement
        WHERE r.isEnabled = true
          AND r.id IN (
            SELECT rd.notificationRoutine.id FROM RoutineDetail rd
            WHERE rd.dayOfWeek = :dayOfWeek AND rd.time = :time
          )
    """)
    List<NotificationRoutine> findRoutinesToSend(@Param("dayOfWeek") RoutineDayOfWeek dayOfWeek,
                                                 @Param("time") LocalTime time);

    // 사용자와 루틴 ID 모두 일치하는 루틴만 조회
    Optional<NotificationRoutine> findByIdAndUserId(Long id, Long userId);

    // (선택) 단건 수정 분기에 사용할 수 있는 fetch 메서드
    @Query("""
        SELECT r FROM NotificationRoutine r
        LEFT JOIN FETCH r.customSupplement
        LEFT JOIN FETCH r.supplement
        WHERE r.id = :id
    """)
    Optional<NotificationRoutine> findByIdWithTargets(@Param("id") Long id);

    boolean existsByCustomSupplementId(Long customSupplementId);

    void deleteAllByUser(User user);
}