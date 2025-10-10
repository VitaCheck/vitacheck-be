package com.vitacheck.Intake.repository;

import com.vitacheck.Intake.domain.IntakeRecord;
import com.vitacheck.Notification.domain.NotificationRoutine;
import com.vitacheck.user.domain.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;

@Repository
public interface IntakeRecordRepository extends JpaRepository<IntakeRecord, Long> {

    Optional<IntakeRecord> findByUserAndNotificationRoutineAndDate(User user,
                                                                   NotificationRoutine notificationRoutine,
                                                                   LocalDate date);

    boolean existsByNotificationRoutineIdAndUserIdAndDate(Long routineId, Long userId, LocalDate date);

    boolean existsByNotificationRoutineAndUserAndDateAndIsTaken(NotificationRoutine routine, User user, LocalDate date, boolean isTaken);

    void deleteAllByUser(User user);
}