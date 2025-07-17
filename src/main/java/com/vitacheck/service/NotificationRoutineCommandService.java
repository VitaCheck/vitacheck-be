package com.vitacheck.service;

import com.amazonaws.services.kms.model.NotFoundException;
import com.vitacheck.domain.*;
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

        // 1. 중복 등록 검사
        boolean isDuplicate = notificationRoutineRepository.existsDuplicateRoutine(
                userId,
                request.getSupplementId(),
                request.getDaysOfWeek(),
                request.getTimes()
        );
        if (isDuplicate) {
            throw new CustomException(ErrorCode.DUPLICATED_ROUTINE);
        }

        // 2. 사용자 조회
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        // 3. 영양제 존재 여부 검증
        Supplement supplement = supplementRepository.findById(request.getSupplementId())
                .orElseThrow(() -> new CustomException(ErrorCode.SUPPLEMENT_NOT_FOUND));

        // 4. 루틴 엔티티 생성
        NotificationRoutine routine = NotificationRoutine.builder()
                .user(user)
                .supplement(supplement)
                .build();

        // 5. 요일 추가
        for (RoutineDayOfWeek dayOfWeek : request.getDaysOfWeek()) {
            RoutineDay day = RoutineDay.builder()
                    .dayOfWeek(dayOfWeek)
                    .build();
            routine.addRoutineDay(day);
        }

        // 6. 시간 추가
        for (LocalTime time : request.getTimes()) {
            RoutineTime routineTime = RoutineTime.builder()
                    .time(time)
                    .build();
            routine.addRoutineTime(routineTime);
        }

        // 7. 저장 (cascade로 하위 엔티티도 자동 저장)
        NotificationRoutine saved = notificationRoutineRepository.save(routine);

        // 8. 응답 DTO 반환
        return RoutineRegisterResponseDto.builder()
                .routineId(saved.getId())
                .supplementId(saved.getSupplement().getId())
                .daysOfWeek(saved.getRoutineDays().stream()
                        .map(RoutineDay::getDayOfWeek)
                        .toList())
                .times(saved.getRoutineTimes().stream()
                        .map(RoutineTime::getTime)
                        .toList())
                .build();
    }
}
