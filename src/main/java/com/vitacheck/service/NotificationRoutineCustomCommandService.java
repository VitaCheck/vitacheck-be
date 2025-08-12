package com.vitacheck.service;

import com.vitacheck.domain.CustomSupplement;
import com.vitacheck.domain.RoutineDetail;
import com.vitacheck.domain.notification.NotificationRoutine;
import com.vitacheck.domain.user.User;
import com.vitacheck.dto.CustomRoutineUpsertRequestDto;
import com.vitacheck.dto.RoutineRegisterResponseDto;
import com.vitacheck.global.apiPayload.CustomException;
import com.vitacheck.global.apiPayload.code.ErrorCode;
import com.vitacheck.repository.CustomSupplementRepository;
import com.vitacheck.repository.NotificationRoutineRepository;
import com.vitacheck.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class NotificationRoutineCustomCommandService {

    private final NotificationRoutineRepository routineRepo;
    private final CustomSupplementRepository customRepo;
    private final UserRepository userRepo;

    public RoutineRegisterResponseDto upsert(Long userId, CustomRoutineUpsertRequestDto req) {
        User user = userRepo.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        NotificationRoutine routine;

        if (req.getNotificationRoutineId() == null) {
            // ===== 생성 =====
            // 동일 이름 있으면 재사용, 없으면 생성
            CustomSupplement cs = customRepo.findByUserIdAndName(userId, req.getName())
                    .orElseGet(() -> customRepo.save(
                            CustomSupplement.builder()
                                    .user(user)
                                    .name(req.getName())
                                    .imageUrl(blankToNull(req.getImageUrl()))
                                    .build()
                    ));
            // 재사용 케이스라도 최신 값 반영
            cs.update(req.getName(), blankToNull(req.getImageUrl()));

            routine = NotificationRoutine.forCustom(user, cs);

            // 스케줄 구성
            routine.clearRoutineDetails();
            req.getSchedules().forEach(s -> {
                RoutineDetail d = RoutineDetail.builder()
                        .dayOfWeek(s.getDayOfWeek())
                        .time(s.getTime())
                        .build();
                routine.addRoutineDetail(d);
            });

            routineRepo.save(routine); // 생성은 save 필요

        } else {
            // ===== 수정 =====
            routine = routineRepo.findByIdWithTargets(req.getNotificationRoutineId())
                    .orElseThrow(() -> new CustomException(ErrorCode.ROUTINE_NOT_FOUND));

            if (!routine.isOwner(userId)) throw new CustomException(ErrorCode.UNAUTHORIZED);
            if (!routine.isCustom()) throw new CustomException(ErrorCode.INVALID_REQUEST);

            CustomSupplement cs = routine.getCustomSupplement();

            // 이름 충돌 방지: 같은 유저의 다른 cs가 동일 이름인지 체크
            String newName = req.getName();
            customRepo.findByUserIdAndName(userId, newName)
                    .filter(other -> !other.getId().equals(cs.getId()))
                    .ifPresent(conflict -> { throw new CustomException(ErrorCode.DUPLICATED_ROUTINE); });

            // 기존 cs 엔티티 '값만' 변경 (INSERT 금지)
            cs.update(newName, blankToNull(req.getImageUrl()));

            // 스케줄 교체
            routine.clearRoutineDetails();
            req.getSchedules().forEach(s -> {
                RoutineDetail d = RoutineDetail.builder()
                        .dayOfWeek(s.getDayOfWeek())
                        .time(s.getTime())
                        .build();
                routine.addRoutineDetail(d);
            });
            // @Transactional + 영속 상태 → save 호출 불필요
        }

        return RoutineRegisterResponseDto.builder()
                .notificationRoutineId(routine.getId())
                .supplementId(null) // 커스텀은 카탈로그 ID 없음
                .supplementName(routine.getCustomSupplement().getName())
                .supplementImageUrl(routine.getCustomSupplement().getImageUrl())
                .schedules(routine.getRoutineDetails().stream()
                        .map(d -> RoutineRegisterResponseDto.ScheduleResponse.builder()
                                .dayOfWeek(d.getDayOfWeek())
                                .time(d.getTime())
                                .build())
                        .toList())
                .build();
    }

    private String blankToNull(String s) {
        return (s == null || s.isBlank()) ? null : s;
    }
}