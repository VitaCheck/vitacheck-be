package com.vitacheck.service;

import com.vitacheck.Activity.repository.IngredientLikeRepository;
import com.vitacheck.Activity.repository.SupplementLikeRepository;
import com.vitacheck.repository.*;
import com.vitacheck.user.domain.User;
import com.vitacheck.user.domain.UserStatus;
import com.vitacheck.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserCleanUpService {

    private final UserRepository userRepository;
    private final IngredientLikeRepository ingredientLikeRepository;
    private final SupplementLikeRepository supplementLikeRepository;
    private final NotificationSettingsRepository notificationSettingsRepository;
    private final NotificationRoutineRepository notificationRoutineRepository;
    private final CustomSupplementRepository customSupplementRepository;
    private final UserTermsAgreementRepository userTermsAgreementRepository;
    private final SearchLogRepository searchLogRepository;
    private final IntakeRecordRepository intakeRecordRepository;

    @Scheduled(cron = "0 0 4 * * *") // 30일
    @Transactional
    public void cleanUpDeletedUser() {
        LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(30);
        List<User> userToDeleted = userRepository.findByStatusAndUpdatedAtBefore(UserStatus.DELETED, thirtyDaysAgo);

        if (userToDeleted.isEmpty()) {
            log.info("영구 삭제할 유저가 없습니다.");
            return;
        }

        log.info("{}명의 사용자를 영구 삭제합니다.", userToDeleted.size());
        for (User user : userToDeleted) {
            deleteUserPermanently(user);
        }
    }

    private void deleteUserPermanently(User user) {
        // 모든 연관 데이터 삭제 (Hard Delete)
        intakeRecordRepository.deleteAllByUser(user);
        notificationRoutineRepository.deleteAllByUser(user);
        customSupplementRepository.deleteAllByUser(user);
        notificationSettingsRepository.deleteAllByUser(user);
        ingredientLikeRepository.deleteAllByUser(user);
        supplementLikeRepository.deleteAllByUser(user);
        userTermsAgreementRepository.deleteAllByUser(user);
        searchLogRepository.deleteAllByUserId(user.getId());

        // 마지막으로 사용자 정보 영구 삭제
        userRepository.delete(user);
        log.info("사용자 ID: {}의 모든 정보가 영구 삭제되었습니다.", user.getId());
    }
}