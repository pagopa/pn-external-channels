package it.pagopa.pn.externalchannels.sqs.consumer.handler.internal;

import it.pagopa.pn.externalchannels.dao.EventCodeDocumentsDao;
import it.pagopa.pn.externalchannels.dao.NotificationProgressDao;
import it.pagopa.pn.externalchannels.dto.CodeTimeToSend;
import it.pagopa.pn.externalchannels.dto.NotificationProgress;
import it.pagopa.pn.externalchannels.mapper.PaperProgressStatusEventToConsolidatorePaperProgressStatusEvent;
import it.pagopa.pn.externalchannels.middleware.InternalSendClient;
import it.pagopa.pn.externalchannels.middleware.ProducerHandler;
import it.pagopa.pn.externalchannels.middleware.extchannelwebhook.ExtChannelWebhookClient;
import it.pagopa.pn.externalchannels.model.SingleStatusUpdate;
import it.pagopa.pn.externalchannels.service.HistoricalRequestService;
import it.pagopa.pn.externalchannels.service.SafeStorageService;
import it.pagopa.pn.externalchannels.util.EventMessageUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.LinkedList;

@Component
@RequiredArgsConstructor
@Slf4j
public class InternalEventHandler {

    private final NotificationProgressDao dao;

    private final EventCodeDocumentsDao eventCodeDocumentsDao;

    private final ProducerHandler producerHandler;

    private final SafeStorageService safeStorageService;

    private final HistoricalRequestService historicalRequestService;

    private final ExtChannelWebhookClient extChannelWebhookClient;

    private final InternalSendClient internalSendClient;

    public void handleMessage(NotificationProgress notificationProgress) {
        Instant now = Instant.now();
        log.trace("[{}] Evaluating if process: {}", notificationProgress.getIun(), notificationProgress);
        if (isTimeToSendMessage(now, notificationProgress)) {
            log.info("[{}] Processing notificationProgress: {}", notificationProgress.getIun(), notificationProgress);
            saveHistoricalDateInCache(notificationProgress);
            sendMessage(notificationProgress);
            notificationProgress.setLastMessageSentTimestamp(Instant.now());

            log.info("[{}] Value of queue after message sent: {}", notificationProgress.getIun(), notificationProgress.getCodeTimeToSendQueue());
            if (notificationProgress.getCodeTimeToSendQueue().isEmpty()) {
                dao.delete(notificationProgress.getIun(), notificationProgress.getDestinationAddress());
                log.info("[{}] Deleted message with requestId: {}", notificationProgress.getIun(), notificationProgress.getRequestId());
            }
            else {
                dao.put(notificationProgress);
                //metto in coda il nuovo notificationProgress, con un CodeTimeToSend in meno
                internalSendClient.sendNotification(notificationProgress);
            }
        }
        else {
            //rimetto in coda
            internalSendClient.sendNotification(notificationProgress);
        }
    }

    private boolean isTimeToSendMessage(Instant now, NotificationProgress notificationProgress) {
        Instant messageTimestamp = notificationProgress.getLastMessageSentTimestamp() == null ? notificationProgress.getCreateMessageTimestamp()
                : notificationProgress.getLastMessageSentTimestamp();

        log.debug("[{}] Compare date- messageTimestamp: {}, now: {}", notificationProgress.getIun(), messageTimestamp, now);
        CodeTimeToSend codeTimeToSend = new LinkedList<>(notificationProgress.getCodeTimeToSendQueue()).peek();
        log.debug("[{}] CodeTimeToSend: {}", notificationProgress.getIun(), codeTimeToSend);
        assert codeTimeToSend != null;
        log.debug("[{}] CodeTimeToSend time value: {}", notificationProgress.getIun(), codeTimeToSend.getTime());
        long seconds = codeTimeToSend.getTime().getSeconds();
        Instant messageTimestampPlusSeconds = messageTimestamp.plusSeconds(seconds);

        return now.getEpochSecond() >= messageTimestampPlusSeconds.getEpochSecond();
    }

    private void sendMessage(NotificationProgress notificationProgress) {

        SingleStatusUpdate eventMessage = EventMessageUtil.buildMessageEvent(notificationProgress, safeStorageService, eventCodeDocumentsDao);

        if (notificationProgress.getOutput() == NotificationProgress.PROGRESS_OUTPUT_CHANNEL.WEBHOOK_EXT_CHANNEL)
        {
            extChannelWebhookClient.sendPaperProgressStatusRequest(notificationProgress, PaperProgressStatusEventToConsolidatorePaperProgressStatusEvent.map(eventMessage.getAnalogMail()));
        }
        else
        {
            producerHandler.sendToQueue(notificationProgress, eventMessage);
        }

        notificationProgress.setLastMessageSentTimestamp(Instant.now());
    }

    private void saveHistoricalDateInCache(NotificationProgress notificationProgress) {
        log.debug("[{}] Saving historical date in cache", notificationProgress);
        String iun = notificationProgress.getIun();
        String requestId = notificationProgress.getRequestId();
        CodeTimeToSend codeTimeToSend = new LinkedList<>(notificationProgress.getCodeTimeToSendQueue()).peek();
        assert codeTimeToSend != null;
        historicalRequestService.save(iun, requestId, codeTimeToSend.getCode());
    }
}
