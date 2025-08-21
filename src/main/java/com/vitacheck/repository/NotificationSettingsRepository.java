package com.vitacheck.repository;

import com.vitacheck.domain.notification.NotificationChannel;
import com.vitacheck.domain.notification.NotificationSettings;
import com.vitacheck.domain.notification.NotificationType;
import com.vitacheck.domain.user.User;
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
