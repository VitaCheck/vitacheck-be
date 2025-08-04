package com.vitacheck.service;

import com.vitacheck.domain.*;
import com.vitacheck.domain.notification.NotificationRoutine;
import com.vitacheck.domain.user.User;
import com.vitacheck.dto.RoutineRegisterRequestDto;
import com.vitacheck.dto.RoutineRegisterResponseDto;
import com.vitacheck.global.apiPayload.CustomException;
import com.vitacheck.global.apiPayload.code.ErrorCode;
import com.vitacheck.repository.NotificationRoutineRepository;
import com.vitacheck.repository.SupplementRepository;
import com.vitacheck.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalTime;

@Service
@RequiredArgsConstructor
@Transactional
public class NotificationRoutineCommandService {

    private final NotificationRoutineRepository notificationRoutineRepository;
    private final SupplementRepository supplementRepository;
    private final UserRepository userRepository; // 또는 AuthContext에서 가져오는 방식

    public RoutineRegisterResponseDto registerRoutine(Long userId, RoutineRegisterRequestDto request) {

        // 사용자 조회
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        // 영양제 존재 여부 검증
        Supplement supplement = supplementRepository.findById(request.getSupplementId())
                .orElseThrow(() -> new CustomException(ErrorCode.SUPPLEMENT_NOT_FOUND));

        NotificationRoutine routine;

        if (request.getNotificationRoutineId() != null) {
            // ---------------------- ✏️ 수정 로직 ----------------------
            routine = notificationRoutineRepository.findByIdAndUserId(
                    request.getNotificationRoutineId(), userId
            ).orElseThrow(() -> new CustomException(ErrorCode.ROUTINE_NOT_FOUND));

            // 기존 요일/시간 모두 제거
            routine.getRoutineDays().clear();
            routine.getRoutineTimes().clear();

        } else {
            // ---------------------- ➕ 등록 로직 ----------------------
            // 중복 등록 검사
            boolean isDuplicate = notificationRoutineRepository.existsDuplicateRoutine(
                    userId,
                    request.getSupplementId(),
                    request.getDaysOfWeek(),
                    request.getTimes()
            );
            if (isDuplicate) {
                throw new CustomException(ErrorCode.DUPLICATED_ROUTINE);
            }

            routine = NotificationRoutine.builder()
                    .user(user)
                    .supplement(supplement)
                    .build();
        }

        // 공통: 요일 추가
        for (RoutineDayOfWeek dayOfWeek : request.getDaysOfWeek()) {
            RoutineDay day = RoutineDay.builder()
                    .dayOfWeek(dayOfWeek)
                    .build();
            routine.addRoutineDay(day);
        }

        // 공통: 시간 추가
        for (LocalTime time : request.getTimes()) {
            RoutineTime routineTime = RoutineTime.builder()
                    .time(time)
                    .build();
            routine.addRoutineTime(routineTime);
        }

        // 저장
        NotificationRoutine saved = notificationRoutineRepository.save(routine);

        // 응답 DTO 반환
        return RoutineRegisterResponseDto.builder()
                .notificationRoutineId(saved.getId())
                .supplementId(supplement.getId())
                .supplementName(supplement.getName())
                .supplementImageUrl(supplement.getImageUrl())
                .daysOfWeek(saved.getRoutineDays().stream()
                        .map(RoutineDay::getDayOfWeek)
                        .toList())
                .times(saved.getRoutineTimes().stream()
                        .map(RoutineTime::getTime)
                        .toList())
                .build();
    }


    public void deleteRoutine(Long userId, Long routineId) {
        NotificationRoutine routine = notificationRoutineRepository.findById(routineId)
                .orElseThrow(() -> new CustomException(ErrorCode.ROUTINE_NOT_FOUND));

        // 본인 루틴이 아닌 경우
        if (!routine.getUser().getId().equals(userId)) {
            throw new CustomException(ErrorCode.ROUTINE_NOT_FOUND);
        }

        notificationRoutineRepository.delete(routine);
    }
}
