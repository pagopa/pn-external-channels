package it.pagopa.pn.externalchannels.schedule;

import it.pagopa.pn.externalchannels.dao.NotificationProgressDao;
import it.pagopa.pn.externalchannels.dto.NotificationProgress;
import it.pagopa.pn.externalchannels.event.PnDeliveryPushEmailEvent;
import it.pagopa.pn.externalchannels.event.PnDeliveryPushPaperEvent;
import it.pagopa.pn.externalchannels.event.PnDeliveryPushPecEvent;
import it.pagopa.pn.externalchannels.middleware.DeliveryPushSendClient;
import it.pagopa.pn.externalchannels.model.SingleStatusUpdate;
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

    private final DeliveryPushSendClient deliveryPushSendClient;


    @Scheduled(cron = "${job.cron-expression}")
    public void run() {
        log.debug("[{}] CLOCK!", Instant.now());
        Collection<NotificationProgress> all = dao.findAll();
        Instant now = Instant.now();
        for (NotificationProgress notificationProgress : all) {

            if (isTimeToSendMessage(now, notificationProgress)) {
                sendMessage(notificationProgress);
                notificationProgress.setLastMessageSentTimestamp(Instant.now());

                if (notificationProgress.getCodeToSend().isEmpty()) {
                    dao.delete(notificationProgress.getIun());
                    log.info("Deleted message with requestId: {}", notificationProgress.getRequestId());
                }
            }
        }
    }

    private boolean isTimeToSendMessage(Instant now, NotificationProgress notificationProgress) {
        Instant messageTimestamp = notificationProgress.getLastMessageSentTimestamp() == null ? notificationProgress.getCreateMessageTimestamp()
                : notificationProgress.getLastMessageSentTimestamp();

        long seconds = notificationProgress.getTimeToSend().peek().getSeconds();
        Instant messageTimestampPlusSeconds = messageTimestamp.plusSeconds(seconds);

        return now.getEpochSecond() >= messageTimestampPlusSeconds.getEpochSecond();
    }

    private void sendMessage(NotificationProgress notificationProgress) {
        String code = notificationProgress.getCodeToSend().poll();
        notificationProgress.getTimeToSend().poll();
        String requestId = notificationProgress.getRequestId();
        String iun = notificationProgress.getIun();
        String channel = notificationProgress.getChannel();
        String destinationAddress = notificationProgress.getDestinationAddress();


        SingleStatusUpdate eventMessage = EventMessageUtil.buildMessageEvent(code, requestId, channel, destinationAddress);
        if (EventMessageUtil.LEGAL_CHANNELS.contains(channel)) {
            PnDeliveryPushPecEvent pnDeliveryPushPecEvent = EventMessageUtil.buildPecEvent(eventMessage, iun);
            log.info("Message to send: {}", pnDeliveryPushPecEvent);
            deliveryPushSendClient.sendNotification(pnDeliveryPushPecEvent);
        }
        else if(EventMessageUtil.PAPER_CHANNELS.contains(channel)) {
            PnDeliveryPushPaperEvent pnDeliveryPushPaperEvent = EventMessageUtil.buildPaperEvent(eventMessage, iun);
            log.info("Message to send: {}", pnDeliveryPushPaperEvent);
            deliveryPushSendClient.sendNotification(pnDeliveryPushPaperEvent);
        } else {
            PnDeliveryPushEmailEvent pnDeliveryPushEmailEvent = EventMessageUtil.buildEmailEvent(eventMessage, iun);
            log.info("Message to send: {}", pnDeliveryPushEmailEvent);
            deliveryPushSendClient.sendNotification(pnDeliveryPushEmailEvent);
        }

        notificationProgress.setLastMessageSentTimestamp(Instant.now());
    }
}
