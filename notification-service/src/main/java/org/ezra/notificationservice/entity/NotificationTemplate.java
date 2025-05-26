package org.ezra.notificationservice.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.ezra.notificationservice.enums.NotificationChannel;

import java.time.LocalDateTime;

@Entity
@Table(name = "notification_templates")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationTemplate {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String templateCode;

    @Column(nullable = false)
    private String subjectTemplate;

    @Lob
    @Column(nullable = false, columnDefinition = "TEXT")
    private String bodyTemplate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NotificationChannel defaultChannel;

    private String languageCode;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
