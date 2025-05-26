package org.ezra.notificationservice.service;

import org.ezra.notificationservice.enums.NotificationChannel;

import java.util.Map;

public interface NotificationSender {
    boolean supports(NotificationChannel channel);

    void send(String recipientAddress, String subject, String body, Map<String, String> contextData) throws Exception;
}

