package com.vitacheck.notification.service;

import com.vitacheck.notification.domain.NotificationRoutine;
import com.vitacheck.notification.dto.RoutineRegisterRequestDto;
import com.vitacheck.notification.dto.RoutineRegisterResponseDto;
import com.vitacheck.notification.dto.RoutineResponseDto;
import com.vitacheck.common.exception.CustomException;
import com.vitacheck.common.code.ErrorCode;
import com.vitacheck.notification.domain.RoutineDetail;
import com.vitacheck.product.domain.Supplement.Supplement;
import com.vitacheck.product.repository.SupplementRepository;
import com.vitacheck.notification.repository.CustomSupplementRepository;
import com.vitacheck.notification.repository.IntakeRecordRepository;
import com.vitacheck.notification.repository.NotificationRoutineRepository;
import com.vitacheck.user.domain.User;
import com.vitacheck.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
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
    private final CustomSupplementRepository customSupplementRepository;
    private final IntakeRecordRepository intakeRecordRepository;

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


    @Transactional
    public void deleteRoutine(Long userId, Long routineId) {
        NotificationRoutine routine = notificationRoutineRepository.findByIdWithTargets(routineId)
                .orElseThrow(() -> new CustomException(ErrorCode.ROUTINE_NOT_FOUND));

        if (!routine.getUser().getId().equals(userId)) {
            throw new CustomException(ErrorCode.ROUTINE_NOT_FOUND);
        }

        // 커스텀이면 ID 확보
        Long customId = (routine.isCustom() && routine.getCustomSupplement() != null)
                ? routine.getCustomSupplement().getId()
                : null;

        // 먼저 루틴 삭제
        notificationRoutineRepository.delete(routine);
        // 쿼리 수행 전 flush 보장 (exists 쿼리 전에 삭제 반영)
        notificationRoutineRepository.flush();

        // 커스텀 참조가 더 이상 없으면 커스텀 영양제도 삭제
        if (customId != null && !notificationRoutineRepository.existsByCustomSupplementId(customId)) {
            customSupplementRepository.deleteById(customId);
        }
    }

    @Transactional
    public RoutineResponseDto toggleRoutine(Long userId, Long routineId) {
        NotificationRoutine routine = notificationRoutineRepository.findByIdAndUserId(routineId, userId)
                .orElseThrow(() -> new CustomException(ErrorCode.ROUTINE_NOT_FOUND));

        routine.toggleEnabled();
        notificationRoutineRepository.save(routine);

        boolean isTaken = intakeRecordRepository.existsByNotificationRoutineAndUserAndDateAndIsTaken(
                routine, routine.getUser(), LocalDate.now(), true
        );

        boolean isCustom = routine.isCustom();

        Long supplementId = isCustom ? null : (routine.getSupplement() != null ? routine.getSupplement().getId() : null);
        String supplementName = isCustom ? routine.getCustomSupplement().getName() : routine.getSupplement().getName();
        String supplementImageUrl = isCustom ? routine.getCustomSupplement().getImageUrl() : routine.getSupplement().getImageUrl();

        var scheduleResponses = routine.getRoutineDetails().stream()
                .map(detail -> RoutineResponseDto.ScheduleResponse.builder()
                        .dayOfWeek(detail.getDayOfWeek())
                        .time(detail.getTime())
                        .build())
                .toList();

        return RoutineResponseDto.builder()
                .notificationRoutineId(routine.getId())
                .isCustom(isCustom)
                .supplementId(supplementId)
                .supplementName(supplementName)
                .supplementImageUrl(supplementImageUrl)
                .isTaken(isTaken)
                .isEnabled(routine.isEnabled())
                .schedules(scheduleResponses)
                .build();
    }
}
