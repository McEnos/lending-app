package org.ezra.notificationservice.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.ezra.notificationservice.enums.NotificationChannel;
import org.ezra.notificationservice.service.NotificationSender;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@Slf4j
public class SmsNotificationSender implements NotificationSender {
    @Override
    public boolean supports(NotificationChannel channel) {
        return NotificationChannel.SMS.equals(channel);
    }
    @Override
    public void send(String recipientAddress, String subject, String body, Map<String, String> contextData) throws Exception {
        log.info("--- SIMULATING SMS NOTIFICATION ---");
        log.info("To: {}", recipientAddress);
        log.info("Message: {}", body);
        log.info("--- SMS SIMULATION COMPLETE ---");
    }
}
