package com.vitacheck.Notification.service;

import com.vitacheck.Notification.domain.NotificationChannel;
import com.vitacheck.Notification.domain.NotificationSettings;
import com.vitacheck.Notification.domain.NotificationType;
import com.vitacheck.Notification.dto.NotificationSettingsDto;
import com.vitacheck.common.exception.CustomException;
import com.vitacheck.common.code.ErrorCode;
import com.vitacheck.Notification.repository.NotificationSettingsRepository;
import com.vitacheck.user.domain.User;
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

    /**
     * 사용자의 알림 설정을 조회합니다. 설정이 없으면 빈 리스트를 반환합니다.
     */
    @Transactional(readOnly = true)
    public List<NotificationSettingsDto> getNotificationSettings(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        List<NotificationSettings> settings = notificationSettingsRepository.findByUser(user);

        return settings.stream()
                .map(NotificationSettingsDto::from)
                .collect(Collectors.toList());
    }

    /**
     * 사용자의 특정 알림 설정을 업데이트합니다.
     * 설정이 없는 사용자의 경우, 기본 설정을 먼저 생성한 후 업데이트를 수행합니다.
     */
    @Transactional
    public void updateNotificationSetting(Long userId, NotificationSettingsDto.UpdateRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        List<NotificationSettings> settings = notificationSettingsRepository.findByUser(user);

        // 설정이 없는 신규 유저라면 기본값을 먼저 생성
        if (settings.isEmpty()) {
            settings = createDefaultSettingsForUser(user);
        }

        // 요청에 맞는 설정을 찾아서 상태 변경
        NotificationSettings settingToUpdate = settings.stream()
                .filter(s -> s.getType() == request.getType() && s.getChannel() == request.getChannel())
                .findFirst()
                .orElseThrow(() -> new CustomException(ErrorCode.INVALID_REQUEST));

        settingToUpdate.setIsEnabled(request.isEnabled());

        notificationSettingsRepository.save(settingToUpdate);
    }

    /**
     * 특정 사용자를 위한 기본 알림 설정을 생성하고 DB에 저장합니다.
     * @return 저장된 NotificationSettings 엔티티 리스트를 반환합니다.
     */
    @Transactional
    public List<NotificationSettings> createDefaultSettingsForUser(User user) {
        List<NotificationSettings> defaultSettings = Arrays.stream(NotificationType.values())
                .flatMap(type -> Arrays.stream(NotificationChannel.values())
                        .map(channel -> NotificationSettings.builder()
                                .user(user)
                                .type(type)
                                .channel(channel)
                                .isEnabled(true) // 기본값은 모두 ON
                                .build()))
                .collect(Collectors.toList());

        return notificationSettingsRepository.saveAll(defaultSettings);
    }
}