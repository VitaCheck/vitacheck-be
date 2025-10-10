package com.vitacheck.user.notification.repository;

import com.vitacheck.user.domain.User;
import com.vitacheck.user.notification.domain.NotificationChannel;
import com.vitacheck.user.notification.domain.NotificationSettings;
import com.vitacheck.user.notification.domain.NotificationType;
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
