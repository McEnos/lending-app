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
import org.ezra.notificationservice.service.NotificationSender;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
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
    private final List<NotificationSender> notificationSenders;

    private static final Pattern PLACEHOLDER_PATTERN = Pattern.compile("\\{(.+?)}");

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
            log.warn("No template found for templateCode/eventType: '{}'. Logging raw event. EventId: {}",
                    eventDto.getEventType(), eventDto.getEventId());
            logBuilder.body("No template found. Raw payload: " + (eventDto.getPayload() != null ? eventDto.getPayload().toString() : "null"))
                    .channel(NotificationChannel.PUSH)
                    .status(NotificationStatus.FAILED)
                    .failureReason("Template not found: " + eventDto.getEventType());
            logRepository.save(logBuilder.build());
            return;
        }

        NotificationTemplate template = templateOpt.get();
        logBuilder.templateUsed(template);
        NotificationChannel targetChannel = template.getDefaultChannel();
        logBuilder.channel(targetChannel);

        String recipientAddress = determineRecipientAddress(targetChannel, eventDto.getCustomerId(), eventDto.getPayload());
        if (recipientAddress == null || recipientAddress.isBlank()) {
            log.warn("Could not determine recipient address for customerId: {}, channel: {}. EventId: {}",
                    eventDto.getCustomerId(), targetChannel, eventDto.getEventId());
            logBuilder.status(NotificationStatus.FAILED)
                    .failureReason("Recipient address not found or not provided for channel " + targetChannel);
            logRepository.save(logBuilder.build());
            return;
        }
        logBuilder.recipientAddress(recipientAddress);
        String renderedSubject = renderTemplate(template.getSubjectTemplate(), eventDto.getPayload());
        String renderedBody = renderTemplate(template.getBodyTemplate(), eventDto.getPayload());

        logBuilder.subject(renderedSubject);
        logBuilder.body(renderedBody);
        boolean sentSuccessfully = false;
        String sendFailureReason = "No suitable sender found for channel: " + targetChannel;

        for (NotificationSender sender : notificationSenders) {
            if (sender.supports(targetChannel)) {
                try {
                    sender.send(recipientAddress, renderedSubject, renderedBody, eventDto.getPayload());
                    sentSuccessfully = true;
                    log.info("SIMULATED SEND via {}: Channel='{}', To='{}', Subject='{}', EventId='{}'",
                            sender.getClass().getSimpleName(), targetChannel, recipientAddress, renderedSubject, eventDto.getEventId());
                    break; // Sent successfully by one sender
                } catch (Exception e) {
                    log.error("Error sending notification via {}: Channel='{}', To='{}', EventId='{}': {}",
                            sender.getClass().getSimpleName(), targetChannel, recipientAddress, eventDto.getEventId(), e.getMessage(), e);
                    sendFailureReason = "Failed to send via " + sender.getClass().getSimpleName() + ": " + e.getMessage();
                }
            }
        }
        if (sentSuccessfully) {
            logBuilder.status(NotificationStatus.SIMULATED);
            logBuilder.sentAt(LocalDateTime.now());
        } else {
            logBuilder.status(NotificationStatus.FAILED);
            logBuilder.failureReason(sendFailureReason);
        }
        logRepository.save(logBuilder.build());
        log.info("Notification log with status {} saved for EventId: {}", logBuilder.build().getStatus(), eventDto.getEventId());
    }

    private String renderTemplate(String templateString, Map<String, String> parameters) {
        if (templateString == null || templateString.isEmpty()) {
            return "";
        }
        if (parameters == null || parameters.isEmpty()) {
            Matcher earlyMatcher = PLACEHOLDER_PATTERN.matcher(templateString);
            if (!earlyMatcher.find()) {
                return templateString;
            }
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

    private String determineRecipientAddress(NotificationChannel channel, Long customerId, Map<String, String> payload) {
        return switch (channel) {
            case EMAIL -> payload.getOrDefault("customerEmail", "customer_" + customerId + "@simulated-email.com");
            case SMS -> payload.getOrDefault("customerPhoneNumber", "+10000000000");
            case PUSH -> "ConsoleLogForCustomer_" + customerId;
            default -> null;
        };
    }
}
