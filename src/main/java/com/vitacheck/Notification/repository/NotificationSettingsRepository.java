package com.vitacheck.Notification.repository;

import com.vitacheck.Notification.domain.NotificationChannel;
import com.vitacheck.Notification.domain.NotificationSettings;
import com.vitacheck.Notification.domain.NotificationType;
import com.vitacheck.user.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface NotificationSettingsRepository extends JpaRepository<NotificationSettings, Long> {
    List<NotificationSettings> findByUser(User user);
    Optional<NotificationSettings> findByUserAndTypeAndChannel(User user, NotificationType type, NotificationChannel channel);
    void deleteAllByUser(User user);
}
