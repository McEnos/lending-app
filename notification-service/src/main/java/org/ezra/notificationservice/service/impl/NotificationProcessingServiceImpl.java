package org.ezra.notificationservice.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.ezra.notificationservice.entity.NotificationLog;
import org.ezra.notificationservice.entity.NotificationTemplate;
import org.ezra.notificationservice.enums.NotificationChannel;
import org.ezra.notificationservice.enums.NotificationStatus;
import org.ezra.notificationservice.event.NotificationEventDto;
import org.ezra.notificationservice.repository.NotificationLogRepository;
import org.ezra.notificationservice.repository.NotificationTemplateRepository;
import org.ezra.notificationservice.service.NotificationProcessingService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationProcessingServiceImpl implements NotificationProcessingService {
    private final NotificationTemplateRepository templateRepository;
    private final NotificationLogRepository logRepository;

    private static final Pattern PLACEHOLDER_PATTERN = Pattern.compile("\\{(.+?)\\}");

    @Override
    @Transactional
    public void processNotificationEvent(NotificationEventDto eventDto) {
        log.info("Processing notification event: Type='{}', CustomerId='{}', EventId='{}'",
                eventDto.getEventType(), eventDto.getCustomerId(), eventDto.getEventId());

        NotificationLog.NotificationLogBuilder logBuilder = NotificationLog.builder()
                .customerId(eventDto.getCustomerId())
                .eventType(eventDto.getEventType())
                .parameters(eventDto.getPayload())
                .processedAt(LocalDateTime.now())
                .status(NotificationStatus.PENDING);

        Optional<NotificationTemplate> templateOpt = templateRepository.findByTemplateCode(eventDto.getEventType());

        if (templateOpt.isEmpty()) {
            log.warn("No template found for templateCode/eventType: {}. Logging raw event.", eventDto.getEventType());
            logBuilder.body("No template found. Raw payload: " + eventDto.getPayload().toString())
                    .channel(NotificationChannel.CONSOLE)
                    .status(NotificationStatus.FAILED)
                    .failureReason("Template not found: " + eventDto.getEventType());
            logRepository.save(logBuilder.build());
            return;
        }

        NotificationTemplate template = templateOpt.get();
        logBuilder.templateUsed(template);
        logBuilder.channel(template.getDefaultChannel());
        String recipientAddress = eventDto.getPayload().getOrDefault("customerEmail",
                eventDto.getPayload().getOrDefault("customerPhoneNumber",
                        "customer_" + eventDto.getCustomerId() + "@simulated.com"));
        logBuilder.recipientAddress(recipientAddress);


        String renderedSubject = renderTemplate(template.getSubjectTemplate(), eventDto.getPayload());
        String renderedBody = renderTemplate(template.getBodyTemplate(), eventDto.getPayload());

        logBuilder.subject(renderedSubject);
        logBuilder.body(renderedBody);

        boolean sentSuccessfully = simulateSend(template.getDefaultChannel(), recipientAddress, renderedSubject, renderedBody);

        if (sentSuccessfully) {
            logBuilder.status(NotificationStatus.SIMULATED);
            logBuilder.sentAt(LocalDateTime.now());
            log.info("SIMULATED SEND: Channel='{}', To='{}', Subject='{}'",
                    template.getDefaultChannel(), recipientAddress, renderedSubject);
        } else {
            logBuilder.status(NotificationStatus.FAILED);
            logBuilder.failureReason("Simulated send failure or channel not supported for simulation.");
            log.error("SIMULATED SEND FAILED: Channel='{}', To='{}'", template.getDefaultChannel(), recipientAddress);
        }

        logRepository.save(logBuilder.build());
        log.info("Notification log saved for EventId: {}", eventDto.getEventId());
    }

    private String renderTemplate(String templateString, Map<String, String> parameters) {
        if (templateString == null || parameters == null) {
            return templateString;
        }
        StringBuilder sb = new StringBuilder();
        Matcher matcher = PLACEHOLDER_PATTERN.matcher(templateString);
        while (matcher.find()) {
            String placeholderKey = matcher.group(1);
            String replacement = parameters.getOrDefault(placeholderKey, matcher.group(0));
            matcher.appendReplacement(sb, Matcher.quoteReplacement(replacement));
        }
        matcher.appendTail(sb);
        return sb.toString();
    }

    private boolean simulateSend(NotificationChannel channel, String recipient, String subject, String body) {
        switch (channel) {
            case EMAIL:
                log.info("--- SIMULATING EMAIL ---");
                log.info("To: {}", recipient);
                log.info("Subject: {}", subject);
                log.info("Body: \n{}", body);
                log.info("--- END SIMULATING EMAIL ---");
                return true;
            case SMS:
                log.info("--- SIMULATING SMS ---");
                log.info("To: {}", recipient);
                log.info("Message: {}", body);
                log.info("--- END SIMULATING SMS ---");
                return true;
            case CONSOLE:
                log.info("--- CONSOLE NOTIFICATION ---");
                log.info("Recipient Context: {}", recipient);
                log.info("Subject: {}", subject);
                log.info("Body: \n{}", body);
                log.info("--- END CONSOLE NOTIFICATION ---");
                return true;
            case PUSH:
                log.info("--- SIMULATING PUSH NOTIFICATION ---");
                log.info("To Device Token (context): {}", recipient);
                log.info("Title: {}", subject);
                log.info("Body: {}", body);
                log.info("--- END SIMULATING PUSH NOTIFICATION ---");
                return true;
            default:
                log.warn("Simulation for channel {} not implemented.", channel);
                return false;
        }
    }
}
