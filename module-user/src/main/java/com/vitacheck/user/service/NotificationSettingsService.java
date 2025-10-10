package com.vitacheck.user.service;

import com.vitacheck.common.code.ErrorCode;
import com.vitacheck.common.exception.CustomException;
import com.vitacheck.user.domain.user.User;
import com.vitacheck.user.domain.notification.NotificationChannel;
import com.vitacheck.user.domain.notification.NotificationSettings;
import com.vitacheck.user.domain.notification.NotificationType;
import com.vitacheck.user.dto.NotificationSettingsDto;
import com.vitacheck.user.repository.NotificationSettingsRepository;
import com.vitacheck.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class NotificationSettingsService {

    private final NotificationSettingsRepository notificationSettingsRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public List<NotificationSettingsDto> getNotificationSettings(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
        List<NotificationSettings> settings = notificationSettingsRepository.findByUser(user);
        return settings.stream()
                .map(NotificationSettingsDto::from)
                .collect(Collectors.toList());
    }

    @Transactional
    public void updateNotificationSetting(Long userId, NotificationSettingsDto.UpdateRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        NotificationSettings settingToUpdate = notificationSettingsRepository
                .findByUserAndTypeAndChannel(user, request.getType(), request.getChannel())
                .orElseThrow(() -> new CustomException(ErrorCode.INVALID_REQUEST));

        boolean currentStatus = settingToUpdate.isEnabled();
        settingToUpdate.setIsEnabled(!currentStatus);

        // 변경된 상태를 명시적으로 저장합니다.
        notificationSettingsRepository.save(settingToUpdate);
    }

    // 이 메서드는 UserEventService에서만 호출됩니다.
    @Transactional
    public void createDefaultSettingsForUser(User user) {
        List<NotificationSettings> defaultSettings = Arrays.stream(NotificationType.values())
                .flatMap(type -> Arrays.stream(NotificationChannel.values())
                        .map(channel -> NotificationSettings.builder()
                                .user(user)
                                .type(type)
                                .channel(channel)
                                .isEnabled(true)
                                .build()))
                .collect(Collectors.toList());
        notificationSettingsRepository.saveAll(defaultSettings);
    }
}