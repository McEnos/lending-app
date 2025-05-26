package org.ezra.notificationservice.repository;

import org.ezra.notificationservice.entity.NotificationLog;
import org.ezra.notificationservice.enums.NotificationChannel;
import org.ezra.notificationservice.enums.NotificationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface NotificationLogRepository extends JpaRepository<NotificationLog, Long> {
    Page<NotificationLog> findByCustomerIdOrderByProcessedAtDesc(Long customerId, Pageable pageable);
    List<NotificationLog> findByStatus(NotificationStatus status);
    List<NotificationLog> findByChannel(NotificationChannel channel);
    List<NotificationLog> findBySentAtBetween(LocalDateTime startTime, LocalDateTime endTime);
    Page<NotificationLog> findByEventTypeOrderByProcessedAtDesc(String eventType, Pageable pageable);
}