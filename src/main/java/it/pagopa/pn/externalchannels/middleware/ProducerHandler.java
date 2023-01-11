package it.pagopa.pn.externalchannels.middleware;

import it.pagopa.pn.externalchannels.dto.NotificationProgress;
import it.pagopa.pn.externalchannels.event.PaperChannelEvent;
import it.pagopa.pn.externalchannels.event.PnDeliveryPushEvent;
import it.pagopa.pn.externalchannels.model.SingleStatusUpdate;
import it.pagopa.pn.externalchannels.util.EventMessageUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class ProducerHandler {

    private final DeliveryPushSendClient deliveryPushSendClient;

    private final PaperChannelSendClient paperChannelSendClient;


    public void sendToQueue(NotificationProgress notificationProgress, SingleStatusUpdate eventMessage) {
        if(notificationProgress.getAppSourceName().equals("PAPER_CHANNEL")) {
            PaperChannelEvent event = EventMessageUtil.buildPaperChannelEvent(eventMessage, notificationProgress.getIun());
            log.info("[{}] Message to send to paper-channel: {}", notificationProgress.getIun(), event);
            paperChannelSendClient.sendNotification(event);
            log.debug("[{}] Message sent to paper-channel", notificationProgress.getIun());
        }
        else { //di default scrivo sulla coda di delivery-push
            PnDeliveryPushEvent event = EventMessageUtil.buildDeliveryPushEvent(eventMessage, notificationProgress.getIun());
            log.info("[{}] Message to send to delivery-push: {}", notificationProgress.getIun(), event);
            deliveryPushSendClient.sendNotification(event);
            log.debug("[{}] Message sent to delivery-push", notificationProgress.getIun());
        }
    }

}
