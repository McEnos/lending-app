package org.ezra.notificationservice.mapper;

import org.ezra.notificationservice.dto.NotificationLogResponseDto;
import org.ezra.notificationservice.entity.NotificationLog;
import org.springframework.stereotype.Component;

@Component
public class NotificationLogMapper {
    public NotificationLogResponseDto toDto(NotificationLog log) {
        if (log == null) return null;
        return NotificationLogResponseDto.builder()
                .id(log.getId())
                .customerId(log.getCustomerId())
                .recipientAddress(log.getRecipientAddress())
                .channel(log.getChannel())
                .templateCode(log.getTemplateUsed() != null ? log.getTemplateUsed().getTemplateCode() : null)
                .eventType(log.getEventType())
                .subject(log.getSubject())
                .parameters(log.getParameters())
                .status(log.getStatus())
                .processedAt(log.getProcessedAt())
                .sentAt(log.getSentAt())
                .failureReason(log.getFailureReason())
                .build();
    }
}
