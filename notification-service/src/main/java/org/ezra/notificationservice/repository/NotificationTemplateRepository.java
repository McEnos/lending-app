package org.ezra.notificationservice.repository;

import org.ezra.notificationservice.entity.NotificationTemplate;
import org.ezra.notificationservice.enums.NotificationChannel;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface NotificationTemplateRepository extends JpaRepository<NotificationTemplate, Long> {
    Optional<NotificationTemplate> findByTemplateCode(String templateCode);
    List<NotificationTemplate> findByDefaultChannel(NotificationChannel defaultChannel);
    List<NotificationTemplate> findByLanguageCode(String languageCode);
    boolean existsByTemplateCode(String templateCode);
}