package com.vitacheck.Notification.dto;

import com.vitacheck.Notification.domain.NotificationChannel;
import com.vitacheck.Notification.domain.NotificationSettings;
import com.vitacheck.Notification.domain.NotificationType;
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
