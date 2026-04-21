package it.pagopa.pn.externalchannels.middleware;

import it.pagopa.pn.externalchannels.dto.NotificationProgress;
import it.pagopa.pn.externalchannels.dto.OcrOutputMessage;
import it.pagopa.pn.externalchannels.event.OcrEvent;
import it.pagopa.pn.externalchannels.event.PaperChannelEvent;
import it.pagopa.pn.externalchannels.model.SingleStatusUpdate;
import it.pagopa.pn.externalchannels.util.EventMessageUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class ProducerHandler {

    private final EventBridgeSendClient eventBridgeSendClient;

    private final PaperChannelSendClient paperChannelSendClient;

    private final OcrSendClient ocrSendClient;


    public void sendToQueue(NotificationProgress notificationProgress, SingleStatusUpdate eventMessage) {
        if (notificationProgress.getOutput() == NotificationProgress.PROGRESS_OUTPUT_CHANNEL.QUEUE_PAPER_CHANNEL) {
            PaperChannelEvent event = EventMessageUtil.buildPaperChannelEvent(eventMessage, notificationProgress.getIun());
            log.info("[{}] Message to send to paper-channel: {}", notificationProgress.getIun(), event);
            paperChannelSendClient.sendNotification(event);
            log.debug("[{}] Message sent to paper-channel", notificationProgress.getIun());
        } else {
            log.info("[{}] Message to send to EventBridge: {}", notificationProgress.getIun(), eventMessage);
            eventBridgeSendClient.sendNotification(eventMessage);
            log.debug("[{}] Message sent to EventBridge", notificationProgress.getIun());
        }
    }

    public void sendToQueue(OcrOutputMessage ocrOutputMessage) {
        log.info("[{}] Message to send to ocr: {}", ocrOutputMessage.getCommandId(), ocrOutputMessage);
        OcrEvent event = EventMessageUtil.buildOcrEvent(ocrOutputMessage);
        ocrSendClient.sendOcr(event);
        log.debug("[{}] Message sent to ocr", ocrOutputMessage.getCommandId());
    }

}
