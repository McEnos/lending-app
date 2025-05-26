package org.ezra.lendingservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationRequestDto {
    private Long targetCustomerId;
    private String eventType;
    private Map<String, String> payload;
    private String preferredChannel;
}