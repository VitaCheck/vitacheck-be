package com.vitacheck.service;

import com.vitacheck.domain.notification.NotificationChannel;
import com.vitacheck.domain.notification.NotificationSettings;
import com.vitacheck.domain.notification.NotificationType;
import com.vitacheck.domain.user.User;
import com.vitacheck.dto.NotificationSettingsDto;
import com.vitacheck.global.apiPayload.CustomException;
import com.vitacheck.global.apiPayload.code.ErrorCode;
import com.vitacheck.repository.NotificationSettingsRepository;
import com.vitacheck.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NotificationSettingsService {

    private final NotificationSettingsRepository notificationSettingsRepository;
    private final UserRepository userRepository;

    public List<NotificationSettingsDto> getNotificationSettings(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        List<NotificationSettings> settings = notificationSettingsRepository.findByUser(user);

        // 사용자의 설정값이 DB에 없으면, 기본값(모두 true)을 생성하여 저장
        if (settings.isEmpty()) {
            return createDefaultSettingsForUser(user);
        }
        return settings.stream()
                .map(NotificationSettingsDto::from)
                .collect(Collectors.toList());
    }

    @Transactional
    public void updateNotificationSetting(Long userId, NotificationSettingsDto.UpdateRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        NotificationSettings setting = notificationSettingsRepository
                .findByUserAndTypeAndChannel(user, request.getType(), request.getChannel())
                .orElseThrow(() -> new CustomException(ErrorCode.INVALID_REQUEST)); // 존재하지 않는 설정을 변경하려는 경우

        setting.setIsEnabled(request.isEnabled());

        notificationSettingsRepository.save(setting);
    }

    @Transactional
    public List<NotificationSettingsDto> createDefaultSettingsForUser(User user) {
        List<NotificationSettings> defaultSettings = Arrays.stream(NotificationType.values())
                .flatMap(type -> Arrays.stream(NotificationChannel.values())
                        .map(channel -> NotificationSettings.builder()
                                .user(user)
                                .type(type)
                                .channel(channel)
                                .isEnabled(true) // 기본값은 모두 ON
                                .build()))
                .collect(Collectors.toList());

        notificationSettingsRepository.saveAll(defaultSettings);

        return defaultSettings.stream()
                .map(NotificationSettingsDto::from)
                .collect(Collectors.toList());
    }
}