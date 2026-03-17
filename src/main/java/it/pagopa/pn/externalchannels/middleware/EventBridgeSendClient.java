package it.pagopa.pn.externalchannels.middleware;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import it.pagopa.pn.externalchannels.config.PnExternalChannelsProperties;
import it.pagopa.pn.externalchannels.exception.ExternalChannelsMockException;
import it.pagopa.pn.externalchannels.model.SingleStatusUpdate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.eventbridge.EventBridgeClient;
import software.amazon.awssdk.services.eventbridge.model.PutEventsRequestEntry;
import software.amazon.awssdk.services.eventbridge.model.PutEventsResponse;

import java.time.Instant;

@Component
@RequiredArgsConstructor
@Slf4j
public class EventBridgeSendClient {

    private final EventBridgeClient eventBridgeSyncClient;
    private final ObjectMapper objectMapper;
    private final PnExternalChannelsProperties properties;

    public void sendNotification(SingleStatusUpdate singleStatusUpdate) {
        try {
            PutEventsRequestEntry entry = PutEventsRequestEntry.builder()
                    .time(Instant.now())
                    .source("NOTIFICATION TRACKER")
                    .detailType("ExternalChannelOutcomeEvent")
                    .detail(objectMapper.writeValueAsString(singleStatusUpdate))
                    .eventBusName(properties.getEventBusName())
                    .build();
            log.info("[EventBridge] Publishing event : {}", entry);
            PutEventsResponse response = eventBridgeSyncClient.putEvents(r -> r.entries(entry));
            if (response.failedEntryCount() > 0) {
                log.error("[EventBridge] Failed to publish {} entr{}: {}",
                        response.failedEntryCount(),
                        response.failedEntryCount() == 1 ? "y" : "ies",
                        response.entries());
                throw new ExternalChannelsMockException("EventBridge putEvents failed for " + response.failedEntryCount() + " entries");
            }
            log.info("[EventBridge] Event published successfully to bus '{}', clientId='{}'", properties.getEventBusName(), singleStatusUpdate.getClientId());
        } catch (JsonProcessingException e) {
            throw new ExternalChannelsMockException("Error serializing event for EventBridge", e);
        }
    }
}
