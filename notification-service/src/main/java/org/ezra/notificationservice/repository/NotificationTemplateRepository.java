package org.ezra.notificationservice.repository;

import org.ezra.notificationservice.entity.NotificationTemplate;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface NotificationTemplateRepository extends JpaRepository<NotificationTemplate, Long> {
    Optional<NotificationTemplate> findByTemplateCode(String templateCode);
}