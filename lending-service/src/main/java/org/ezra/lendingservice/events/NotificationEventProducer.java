package org.ezra.lendingservice.events;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationEventProducer {
    @Value("${app.kafka.topic.notification-events}")
    private String notificationEventsTopic;
    private final KafkaTemplate<String, String> kafkaTemplate;

    public void sendNotificationEvent(NotificationEventDto eventDto) {
        if (eventDto.getEventId() == null) {
            eventDto.setEventId(UUID.randomUUID().toString());
        }
        if (eventDto.getTimestamp() == 0) {
            eventDto.setTimestamp(System.currentTimeMillis());
        }
        try {
            log.info("Producing notification event: Type='{}', CustomerId='{}', EventId='{}' to topic '{}'",
                    eventDto.getEventType(), eventDto.getCustomerId(), eventDto.getEventId(), notificationEventsTopic);
            CompletableFuture<SendResult<String, String>> future =
                    kafkaTemplate.send(notificationEventsTopic, eventDto.getEventId(), payloadToString(eventDto));
            future.whenComplete((result, ex) -> {
                if (ex == null) {
                    log.info("Sent message=[{}] with offset=[{}]", eventDto.getEventId(), result.getRecordMetadata().offset());
                } else {
                    log.error("Unable to send message=[{}] due to : {}", eventDto.getEventId(), ex.getMessage(), ex);
                }
            });
        } catch (Exception e) {
            log.error("Unable to send message=[{}]", eventDto.getEventId(), e);
        }


    }


    private String payloadToString(NotificationEventDto notification) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.writeValueAsString(notification);
    }


}
