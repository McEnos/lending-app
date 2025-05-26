package org.ezra.notificationservice.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.ezra.notificationservice.enums.NotificationChannel;
import org.ezra.notificationservice.enums.NotificationStatus;

import java.time.LocalDateTime;
import java.util.Map;

@Entity
@Table(name = "notification_logs")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long customerId;

    private String recipientAddress;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NotificationChannel channel;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "template_id")
    private NotificationTemplate templateUsed;

    private String eventType;

    @Column(length = 500)
    private String subject;
    @Lob
    @Column(columnDefinition = "TEXT")
    private String body;

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "notification_log_parameters", joinColumns = @JoinColumn(name = "notification_log_id"))
    @MapKeyColumn(name = "parameter_name", length = 100)
    @Column(name = "parameter_value", length = 1000)
    private Map<String, String> parameters;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NotificationStatus status;

    private LocalDateTime scheduledAt;
    private LocalDateTime sentAt;
    private LocalDateTime processedAt;

    @Column(length = 1000)
    private String failureReason;
}