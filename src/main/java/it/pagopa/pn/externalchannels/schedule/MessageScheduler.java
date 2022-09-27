package it.pagopa.pn.externalchannels.schedule;

import it.pagopa.pn.api.dto.events.EventPublisher;
import it.pagopa.pn.api.dto.events.StandardEventHeader;
import it.pagopa.pn.externalchannels.dao.NotificationProgressDao;
import it.pagopa.pn.externalchannels.dto.NotificationProgress;
import it.pagopa.pn.externalchannels.event.PnDeliveryPushPecEvent;
import it.pagopa.pn.externalchannels.middleware.DeliveryPushSendClient;
import it.pagopa.pn.externalchannels.model.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class MessageScheduler {

    private static final List<String> ERROR_CODES = List.of("C004", "C008", "C009", "C010");

    private static final String OK_CODE = "C003";

    private final NotificationProgressDao dao;

    private final DeliveryPushSendClient deliveryPushSendClient;


    @Scheduled(cron = "${job.cron-expression}")
    public void run() {
        log.debug("[{}] CLOCK!", Instant.now());
        Collection<NotificationProgress> all = dao.findAll();
        Instant now = Instant.now();
        for (NotificationProgress notificationProgress : all) {
            Instant messageTimestamp = notificationProgress.getLastMessageSentTimestamp() == null ? notificationProgress.getCreateMessageTimestamp()
                    : notificationProgress.getLastMessageSentTimestamp();

            long seconds = notificationProgress.getTimeToSend().peek().getSeconds();
            Instant messageTimestampPlusSeconds = messageTimestamp.plusSeconds(seconds);
            if (now.getEpochSecond() >= messageTimestampPlusSeconds.getEpochSecond()) {
                String code = notificationProgress.getCodeToSend().poll();
                notificationProgress.getTimeToSend().poll();
                String requestId = notificationProgress.getRequestId();
                String iun = notificationProgress.getIun();

                SingleStatusUpdate baseMessageProgressEvent = buildBaseMessageProgressEvent(code, requestId);
                PnDeliveryPushPecEvent pnDeliveryPushPecEvent = buildNotificationInEvent(baseMessageProgressEvent, iun);

                log.info("Message to send: {}", pnDeliveryPushPecEvent);
                deliveryPushSendClient.sendNotification(pnDeliveryPushPecEvent);

                notificationProgress.setLastMessageSentTimestamp(Instant.now());

                if (notificationProgress.getCodeToSend().isEmpty()) {
                    dao.delete(iun);
                    log.info("Deleted message with requestId: {}", iun);
                }
            }
        }
    }

    private SingleStatusUpdate buildBaseMessageProgressEvent(String code, String requestId) {

        return new SingleStatusUpdate()
                .digitalLegal(
                        new LegalMessageSentDetails()
                                .eventCode(LegalMessageSentDetails.EventCodeEnum.fromValue(code))
                                .requestId(requestId)
                                .status(buildStatus(code))
                                .eventTimestamp(OffsetDateTime.now())
                                .generatedMessage(new DigitalMessageReference().system("mock-system").id("mock-" + UUID.randomUUID()))
                );
    }

    private ProgressEventCategory buildStatus(String code) {
        if (code.equals(OK_CODE)) {
            return ProgressEventCategory.OK;
        }
        if (ERROR_CODES.contains(code)) {
            return ProgressEventCategory.ERROR;
        }
        return ProgressEventCategory.PROGRESS;
    }

    private PnDeliveryPushPecEvent buildNotificationInEvent(SingleStatusUpdate event, String iun) {
        return PnDeliveryPushPecEvent.builder()
                .header(StandardEventHeader.builder()
                        .iun(iun)
                        .eventId(event.getDigitalLegal().getRequestId())
                        .eventType("EXTERNAL_CHANNELS_EVENT")
                        .publisher(EventPublisher.EXTERNAL_CHANNELS.name())
                        .createdAt(Instant.now())
                        .build())
                .payload(event)
                .build();
    }
}
