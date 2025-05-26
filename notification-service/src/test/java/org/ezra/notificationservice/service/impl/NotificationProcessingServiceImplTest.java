package org.ezra.notificationservice.service.impl;

import org.ezra.notificationservice.entity.NotificationLog;
import org.ezra.notificationservice.entity.NotificationTemplate;
import org.ezra.notificationservice.enums.NotificationChannel;
import org.ezra.notificationservice.enums.NotificationStatus;
import org.ezra.notificationservice.event.NotificationEventDto;
import org.ezra.notificationservice.repository.NotificationLogRepository;
import org.ezra.notificationservice.repository.NotificationTemplateRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationProcessingServiceImplTest {

    @Mock
    private NotificationTemplateRepository templateRepository;

    @Mock
    private NotificationLogRepository logRepository;

    @InjectMocks
    private NotificationProcessingServiceImpl notificationProcessingService;

    @Captor
    private ArgumentCaptor<NotificationLog> notificationLogCaptor;

    private NotificationEventDto eventDto;
    private NotificationTemplate emailTemplate;

    @BeforeEach
    void setUp() {
        eventDto = NotificationEventDto.builder()
                .eventId("evt-123")
                .eventType("TEST_EVENT_EMAIL")
                .customerId(1L)
                .payload(Map.of("name", "Test User", "item", "Sample Item"))
                .timestamp(System.currentTimeMillis())
                .build();

        emailTemplate = NotificationTemplate.builder()
                .id(1L)
                .templateCode("TEST_EVENT_EMAIL")
                .subjectTemplate("Hello {name}!")
                .bodyTemplate("Your request for {item} is processed.")
                .defaultChannel(NotificationChannel.EMAIL)
                .languageCode("en-US")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    @Test
    void processNotificationEvent_whenTemplateExists_rendersAndLogsSuccessfully() {
        when(templateRepository.findByTemplateCode("TEST_EVENT_EMAIL")).thenReturn(Optional.of(emailTemplate));
        when(logRepository.save(any(NotificationLog.class))).thenAnswer(inv -> inv.getArgument(0));

        notificationProcessingService.processNotificationEvent(eventDto);

        verify(templateRepository, times(1)).findByTemplateCode("TEST_EVENT_EMAIL");
        verify(logRepository, times(1)).save(notificationLogCaptor.capture());

        NotificationLog savedLog = notificationLogCaptor.getValue();
        assertNotNull(savedLog);
        assertEquals(eventDto.getCustomerId(), savedLog.getCustomerId());
        assertEquals(eventDto.getEventType(), savedLog.getEventType());
        assertEquals(emailTemplate, savedLog.getTemplateUsed());
        assertEquals(NotificationChannel.EMAIL, savedLog.getChannel());
        assertEquals("Hello Test User!", savedLog.getSubject());
        assertEquals("Your request for Sample Item is processed.", savedLog.getBody());
        assertEquals(NotificationStatus.SIMULATED, savedLog.getStatus());
        assertNotNull(savedLog.getProcessedAt());
        assertNotNull(savedLog.getSentAt());
        assertEquals(eventDto.getPayload(), savedLog.getParameters());
        assertNull(savedLog.getFailureReason());
    }

    @Test
    void processNotificationEvent_whenTemplateNotFound_logsFailure() {
        when(templateRepository.findByTemplateCode("UNKNOWN_EVENT")).thenReturn(Optional.empty());
        when(logRepository.save(any(NotificationLog.class))).thenAnswer(inv -> inv.getArgument(0));

        NotificationEventDto unknownEventDto = NotificationEventDto.builder()
                .eventId("evt-456")
                .eventType("UNKNOWN_EVENT")
                .customerId(2L)
                .payload(Map.of("data", "some_data"))
                .timestamp(System.currentTimeMillis())
                .build();

        notificationProcessingService.processNotificationEvent(unknownEventDto);

        verify(templateRepository, times(1)).findByTemplateCode("UNKNOWN_EVENT");
        verify(logRepository, times(1)).save(notificationLogCaptor.capture());

        NotificationLog savedLog = notificationLogCaptor.getValue();
        assertNotNull(savedLog);
        assertEquals(unknownEventDto.getCustomerId(), savedLog.getCustomerId());
        assertEquals(unknownEventDto.getEventType(), savedLog.getEventType());
        assertNull(savedLog.getTemplateUsed());
        assertEquals(NotificationChannel.PUSH, savedLog.getChannel());
        assertTrue(savedLog.getBody().contains("No template found. Raw payload:"));
        assertEquals(NotificationStatus.FAILED, savedLog.getStatus());
        assertEquals("Template not found: UNKNOWN_EVENT", savedLog.getFailureReason());
        assertNotNull(savedLog.getProcessedAt());
        assertNull(savedLog.getSentAt());
    }

    @Test
    void processNotificationEvent_placeholderNotInPayload_keepsPlaceholderInRenderedString() {
        NotificationTemplate templateWithMissingPlaceholder = NotificationTemplate.builder()
                .templateCode("EVENT_MISSING_PLACEHOLDER")
                .subjectTemplate("Info: {info}")
                .bodyTemplate("Detail: {detail}, Extra: {extraInfo}")
                .defaultChannel(NotificationChannel.PUSH)
                .build();
        when(templateRepository.findByTemplateCode("EVENT_MISSING_PLACEHOLDER")).thenReturn(Optional.of(templateWithMissingPlaceholder));
        when(logRepository.save(any(NotificationLog.class))).thenAnswer(inv -> inv.getArgument(0));

        NotificationEventDto eventWithPartialPayload = NotificationEventDto.builder()
                .eventId("evt-789")
                .eventType("EVENT_MISSING_PLACEHOLDER")
                .customerId(3L)
                .payload(Map.of("detail", "Known Detail"))
                .build();

        notificationProcessingService.processNotificationEvent(eventWithPartialPayload);

        verify(logRepository).save(notificationLogCaptor.capture());
        NotificationLog savedLog = notificationLogCaptor.getValue();
        assertEquals("Info: {info}", savedLog.getSubject());
        assertEquals("Detail: Known Detail, Extra: {extraInfo}", savedLog.getBody());
        assertEquals(NotificationStatus.SIMULATED, savedLog.getStatus());
    }

    @Test
    void processNotificationEvent_handlesSmsChannelSimulation() {
        NotificationTemplate smsTemplate = NotificationTemplate.builder()
                .templateCode("TEST_EVENT_SMS")
                .subjectTemplate("")
                .bodyTemplate("SMS for {name}: item {item} ready.")
                .defaultChannel(NotificationChannel.SMS)
                .build();
        when(templateRepository.findByTemplateCode("TEST_EVENT_SMS")).thenReturn(Optional.of(smsTemplate));
        when(logRepository.save(any(NotificationLog.class))).thenAnswer(inv -> inv.getArgument(0));

        NotificationEventDto smsEventDto = NotificationEventDto.builder()
                .eventId("evt-sms-001")
                .eventType("TEST_EVENT_SMS")
                .customerId(1L)
                .payload(Map.of("name", "SMS User", "item", "Mobile Plan", "customerPhoneNumber", "1234567890"))
                .build();
        notificationProcessingService.processNotificationEvent(smsEventDto);
        verify(logRepository).save(notificationLogCaptor.capture());
        NotificationLog savedLog = notificationLogCaptor.getValue();
        assertEquals(NotificationChannel.SMS, savedLog.getChannel());
        assertEquals("SMS for SMS User: item Mobile Plan ready.", savedLog.getBody());
        assertEquals("1234567890", savedLog.getRecipientAddress());
        assertEquals(NotificationStatus.SIMULATED, savedLog.getStatus());
    }
}