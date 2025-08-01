package com.vitacheck.repository;

import com.vitacheck.domain.IntakeRecord;
import com.vitacheck.domain.notification.NotificationRoutine;
import com.vitacheck.domain.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;

@Repository
public interface IntakeRecordRepository extends JpaRepository<IntakeRecord, Long> {

    Optional<IntakeRecord> findByUserAndNotificationRoutineAndDate(User user,
                                                                   NotificationRoutine notificationRoutine,
                                                                   LocalDate date);
}