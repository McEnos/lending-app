package org.ezra.notificationservice.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.ezra.notificationservice.service.NotificationProcessingService;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class NotificationEventConsumer {
    private final NotificationProcessingService notificationProcessingService;
    @KafkaListener(
            topics = "${app.kafka.topic.notification-events}",
            groupId = "${spring.kafka.consumer.group-id}",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void consumeNotificationEvent(ConsumerRecord<String, NotificationEventDto> record, @Payload NotificationEventDto eventDto) {
        log.info("<<< Consumed Kafka Message [Key: '{}', EventId: '{}', EventType: '{}', Offset: '{}', Partition: '{}']",
                record.key(),
                eventDto.getEventId(),
                eventDto.getEventType(),
                record.offset(),
                record.partition());

        try {
            notificationProcessingService.processNotificationEvent(eventDto);
            log.info(">>> Successfully processed event: EventId='{}', EventType='{}'", eventDto.getEventId(), eventDto.getEventType());
        } catch (Exception e) {
            log.error("!!! Error processing notification event: EventId='{}', EventType='{}'. Error: {}",
                    eventDto.getEventId(), eventDto.getEventType(), e.getMessage(), e);
        }
    }
}
