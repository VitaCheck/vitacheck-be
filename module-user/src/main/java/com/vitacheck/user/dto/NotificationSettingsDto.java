package com.vitacheck.user.dto;

import com.vitacheck.user.domain.notification.NotificationChannel;
import com.vitacheck.user.domain.notification.NotificationSettings;
import com.vitacheck.user.domain.notification.NotificationType;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
public class NotificationSettingsDto {
    private NotificationType type;
    private NotificationChannel channel;
    private boolean isEnabled;

    public static NotificationSettingsDto from(NotificationSettings settings) {
        return NotificationSettingsDto.builder()
                .type(settings.getType())
                .channel(settings.getChannel())
                .isEnabled(settings.isEnabled())
                .build();
    }

    @Getter
    @NoArgsConstructor
    public static class UpdateRequest {
        private NotificationType type;
        private NotificationChannel channel;
        private boolean isEnabled;
    }
}
