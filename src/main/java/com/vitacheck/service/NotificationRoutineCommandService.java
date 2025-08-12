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
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

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

            routine.clearRoutineDetails();

        } else {
            // ---------------------- ➕ 등록 로직 ----------------------
            // 중복 등록 검사
            List<RoutineDetail> existingDetails = notificationRoutineRepository
                    .findRoutineDetailsByUserIdAndSupplementId(userId, request.getSupplementId());

            Set<String> existingSchedules = existingDetails.stream()
                    .map(detail -> detail.getDayOfWeek().name() + "_" + detail.getTime().toString())
                    .collect(Collectors.toSet());

            boolean isDuplicate = request.getSchedules().stream()
                    .map(schedule -> schedule.getDayOfWeek().name() + "_" + schedule.getTime().toString())
                    .anyMatch(existingSchedules::contains);

            if (isDuplicate) {
                throw new CustomException(ErrorCode.DUPLICATED_ROUTINE);
            }

            routine = NotificationRoutine.builder()
                    .user(user)
                    .supplement(supplement)
                    .build();
        }

        for (RoutineRegisterRequestDto.ScheduledRequest schedule : request.getSchedules()) {
            RoutineDetail detail = RoutineDetail.builder()
                    .dayOfWeek(schedule.getDayOfWeek())
                    .time(schedule.getTime())
                    .build();
            routine.addRoutineDetail(detail);
        }

        // 저장
        NotificationRoutine savedRoutine = notificationRoutineRepository.save(routine);

        // 응답 DTO 반환
        List<RoutineRegisterResponseDto.ScheduleResponse> scheduleResponses = savedRoutine.getRoutineDetails().stream()
                .map(detail -> RoutineRegisterResponseDto.ScheduleResponse.builder()
                        .dayOfWeek(detail.getDayOfWeek())
                        .time(detail.getTime())
                        .build())
                .toList();

        // 2. 변환된 목록을 포함하여 최종 응답 DTO를 생성하고 반환합니다.
        return RoutineRegisterResponseDto.builder()
                .notificationRoutineId(savedRoutine.getId())
                .supplementId(supplement.getId())
                .supplementName(supplement.getName())
                .supplementImageUrl(supplement.getImageUrl())
                .schedules(scheduleResponses)
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
