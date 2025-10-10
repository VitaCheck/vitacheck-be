package com.vitacheck.user.repository;

import com.vitacheck.user.domain.user.User;
import com.vitacheck.user.domain.notification.NotificationChannel;
import com.vitacheck.user.domain.notification.NotificationSettings;
import com.vitacheck.user.domain.notification.NotificationType;
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
