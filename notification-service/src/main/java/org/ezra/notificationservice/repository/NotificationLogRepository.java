package org.ezra.notificationservice.repository;

import org.ezra.notificationservice.entity.NotificationLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationLogRepository extends JpaRepository<NotificationLog, Long> {
}