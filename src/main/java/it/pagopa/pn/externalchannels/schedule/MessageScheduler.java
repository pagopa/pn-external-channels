package it.pagopa.pn.externalchannels.schedule;

import it.pagopa.pn.externalchannels.dao.EventCodeDocumentsDao;
import it.pagopa.pn.externalchannels.dao.NotificationProgressDao;
import it.pagopa.pn.externalchannels.dto.CodeTimeToSend;
import it.pagopa.pn.externalchannels.dto.NotificationProgress;
import it.pagopa.pn.externalchannels.mapper.PaperProgressStatusEventToConsolidatorePaperProgressStatusEvent;
import it.pagopa.pn.externalchannels.middleware.ProducerHandler;
import it.pagopa.pn.externalchannels.middleware.extchannelwebhook.ExtChannelWebhookClient;
import it.pagopa.pn.externalchannels.model.SingleStatusUpdate;
import it.pagopa.pn.externalchannels.service.HistoricalRequestService;
import it.pagopa.pn.externalchannels.service.SafeStorageService;
import it.pagopa.pn.externalchannels.util.EventMessageUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Collection;

@Component
@RequiredArgsConstructor
@Slf4j
public class MessageScheduler {


    private final NotificationProgressDao dao;

    private final EventCodeDocumentsDao eventCodeDocumentsDao;

    private final ProducerHandler producerHandler;

    private final SafeStorageService safeStorageService;

    private final HistoricalRequestService historicalRequestService;

    private final ExtChannelWebhookClient extChannelWebhookClient;


    @Scheduled(cron = "${job.cron-expression}")
    public void run() {
        log.trace("[{}] CLOCK!", Instant.now());
        Collection<NotificationProgress> all = dao.findAll();
        Instant now = Instant.now();
        for (NotificationProgress notificationProgress : all) {
            handleMessage(notificationProgress, now);
        }

    }

    private void handleMessage(NotificationProgress notificationProgress, Instant now) {
        try {
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
            }
        }
        catch (Exception e) {
            log.error(String.format("[%s] Error in handleMessage: %s", notificationProgress.getIun(), e.getMessage()), e);
            //rimuovo il record dal database per non farlo rimanere in uno stato inconsistente
            dao.delete(notificationProgress.getIun(), notificationProgress.getDestinationAddress());
        }

    }

    private boolean isTimeToSendMessage(Instant now, NotificationProgress notificationProgress) {
        Instant messageTimestamp = notificationProgress.getLastMessageSentTimestamp() == null ? notificationProgress.getCreateMessageTimestamp()
                : notificationProgress.getLastMessageSentTimestamp();

        log.debug("[{}] Compare date- messageTimestamp: {}, now: {}", notificationProgress.getIun(), messageTimestamp, now);
        CodeTimeToSend codeTimeToSend = notificationProgress.getCodeTimeToSendQueue().peek();
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
            extChannelWebhookClient.sendPaperProgressStatusRequest(notificationProgress,
                    PaperProgressStatusEventToConsolidatorePaperProgressStatusEvent.map(eventMessage.getAnalogMail()));
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
        CodeTimeToSend codeTimeToSend = notificationProgress.getCodeTimeToSendQueue().peek();
        assert codeTimeToSend != null;
        historicalRequestService.save(iun, requestId, codeTimeToSend.getCode());
    }

    
}
