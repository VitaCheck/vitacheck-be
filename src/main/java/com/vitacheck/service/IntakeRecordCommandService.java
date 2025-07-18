package com.vitacheck.service;

import com.vitacheck.domain.IntakeRecord;
import com.vitacheck.domain.notification.NotificationRoutine;
import com.vitacheck.domain.user.User;
import com.vitacheck.dto.IntakeRecordRequestDto;
import com.vitacheck.dto.IntakeRecordResponseDto;
import com.vitacheck.global.apiPayload.CustomException;
import com.vitacheck.global.apiPayload.code.ErrorCode;
import com.vitacheck.repository.IntakeRecordRepository;
import com.vitacheck.repository.NotificationRoutineRepository;
import com.vitacheck.repository.UserRepository;
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
    public IntakeRecordResponseDto recordIntake(IntakeRecordRequestDto request, Long userId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        // 루틴 소유자 확인을 userId 조건으로 같이
        NotificationRoutine routine = notificationRoutineRepository
                .findByIdAndUserId(request.getNotificationRoutineId(), userId)
                .orElseThrow(() -> new CustomException(ErrorCode.ROUTINE_NOT_FOUND));

        LocalDate today = LocalDate.now();

        // Upsert 방식
        IntakeRecord record = intakeRecordRepository
                .findByUserAndNotificationRoutineAndDate(user, routine, today)
                .map(existing -> {
                    existing.updateIsTaken(request.getIsTaken());
                    return existing;
                })
                .orElseGet(() -> intakeRecordRepository.save(
                        IntakeRecord.builder()
                                .user(user)
                                .notificationRoutine(routine)
                                .date(today)
                                .isTaken(request.getIsTaken())
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