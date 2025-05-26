package org.ezra.notificationservice.service;

import org.ezra.notificationservice.event.NotificationEventDto;

public interface NotificationProcessingService {
    void processNotificationEvent(NotificationEventDto eventDto);
}
