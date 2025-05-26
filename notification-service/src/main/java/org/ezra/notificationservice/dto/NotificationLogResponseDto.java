package org.ezra.notificationservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.ezra.notificationservice.enums.NotificationChannel;
import org.ezra.notificationservice.enums.NotificationStatus;

import java.time.LocalDateTime;
import java.util.Map;


@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationLogResponseDto {
    private Long id;
    private Long customerId;
    private String recipientAddress;
    private NotificationChannel channel;
    private String templateCode;
    private String eventType;
    private String subject;
    private Map<String, String> parameters;
    private NotificationStatus status;
    private LocalDateTime processedAt;
    private LocalDateTime sentAt;
    private String failureReason;
}
