package org.ezra.lendingservice.events;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationEventDto {
    private String eventId;
    private String eventType;
    private Long customerId;
    private Map<String, String> payload;
    private long timestamp;
}