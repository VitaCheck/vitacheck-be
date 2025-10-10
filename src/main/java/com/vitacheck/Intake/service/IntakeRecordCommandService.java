package com.vitacheck.Intake.service;

import com.vitacheck.Intake.domain.IntakeRecord;
import com.vitacheck.Notification.domain.NotificationRoutine;
import com.vitacheck.Intake.dto.IntakeRecordResponseDto;
import com.vitacheck.common.exception.CustomException;
import com.vitacheck.common.code.ErrorCode;
import com.vitacheck.Intake.repository.IntakeRecordRepository;
import com.vitacheck.Notification.repository.NotificationRoutineRepository;
import com.vitacheck.user.domain.user.User;
import com.vitacheck.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class IntakeRecordCommandService {

    private final IntakeRecordRepository intakeRecordRepository;
    private final UserRepository userRepository;
    private final NotificationRoutineRepository notificationRoutineRepository;

    @Transactional
    public IntakeRecordResponseDto toggleIntake(Long notificationRoutineId, Long userId, LocalDate date) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        NotificationRoutine routine = notificationRoutineRepository
                .findByIdAndUserId(notificationRoutineId, userId)
                .orElseThrow(() -> new CustomException(ErrorCode.ROUTINE_NOT_FOUND));

        LocalDate targetDate = (date != null) ? date : LocalDate.now();

        IntakeRecord record = intakeRecordRepository
                .findByUserAndNotificationRoutineAndDate(user, routine, targetDate)
                .map(existing -> {
                    existing.updateIsTaken(!existing.getIsTaken()); // 상태 반전
                    return existing;
                })
                .orElseGet(() -> intakeRecordRepository.save(
                        IntakeRecord.builder()
                                .user(user)
                                .notificationRoutine(routine)
                                .date(targetDate)
                                .isTaken(true) // 첫 등록은 true로 저장
                                .build()
                ));

        return IntakeRecordResponseDto.builder()
                .recordId(record.getId())
                .notificationRoutineId(record.getNotificationRoutine().getId())
                .isTaken(record.getIsTaken())
                .date(record.getDate())
                .build();
    }
}