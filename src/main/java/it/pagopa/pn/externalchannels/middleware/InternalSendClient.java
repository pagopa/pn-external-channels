package it.pagopa.pn.externalchannels.middleware;

import it.pagopa.pn.api.dto.events.EventPublisher;
import it.pagopa.pn.api.dto.events.MomProducer;
import it.pagopa.pn.api.dto.events.StandardEventHeader;
import it.pagopa.pn.externalchannels.dto.NotificationProgress;
import it.pagopa.pn.externalchannels.event.InternalEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.UUID;

import static it.pagopa.pn.externalchannels.event.InternalEvent.INTERNAL_EVENT;
import static it.pagopa.pn.externalchannels.util.EventMessageUtil.MOCK_PREFIX;

@Component
@RequiredArgsConstructor
public class InternalSendClient {

    private final MomProducer<InternalEvent> internalProducer;

    public void sendNotification(NotificationProgress notificationProgress) {
        InternalEvent internalEvent = InternalEvent.builder()
                .header(StandardEventHeader.builder()
                        .iun(notificationProgress.getIun())
                        .eventId(MOCK_PREFIX + UUID.randomUUID())
                        .eventType(INTERNAL_EVENT)
                        .publisher(EventPublisher.EXTERNAL_CHANNELS.name())
                        .createdAt(Instant.now())
                        .build())
                .payload(notificationProgress)
                .build();

        sendNotification(internalEvent);
    }
    public void sendNotification(InternalEvent internalEvent) {
        internalProducer.push(internalEvent);
    }
}
