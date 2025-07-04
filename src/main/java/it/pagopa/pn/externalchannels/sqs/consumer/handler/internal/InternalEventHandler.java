package it.pagopa.pn.externalchannels.sqs.consumer.handler.internal;

import it.pagopa.pn.externalchannels.dao.EventCodeDocumentsDao;
import it.pagopa.pn.externalchannels.dao.NotificationProgressDao;
import it.pagopa.pn.externalchannels.dto.CodeTimeToSend;
import it.pagopa.pn.externalchannels.dto.NotificationProgress;
import it.pagopa.pn.externalchannels.dto.OcrInputMessage;
import it.pagopa.pn.externalchannels.dto.OcrOutputMessage;
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

import static it.pagopa.pn.externalchannels.util.EventMessageUtil.OCR_KO;
import static it.pagopa.pn.externalchannels.util.EventMessageUtil.OCR_PENDING;

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

    public void handleMessage(OcrInputMessage ocrInputMessage) {
        OcrOutputMessage ocrOutputMessage = OcrOutputMessage.builder().build();
        String registeredLetterCode = ocrInputMessage.getData().getDetails().getRegisteredLetterCode();
        log.trace("[{}] Evaluating ocr message for deliveryDriver", ocrInputMessage.getData().getUnifiedDeliveryDriver());
        if (registeredLetterCode.contains(OCR_KO)) {
            log.debug("Creating OCR KO message");
            ocrOutputMessage = buildKoOcrMessage(ocrInputMessage);
        } else if (registeredLetterCode.contains(OCR_PENDING)) {
            log.debug("Creating OCR PENDING message");
            ocrOutputMessage = buildPendingOcrMessage(ocrInputMessage);
        }
        producerHandler.sendToQueue(ocrOutputMessage);
    }

    private OcrOutputMessage buildPendingOcrMessage(OcrInputMessage ocrInputMessage) {
        return OcrOutputMessage.builder()
                .version(ocrInputMessage.getVersion())
                .commandId(ocrInputMessage.getCommandId())
                .commandType(ocrInputMessage.getCommandType())
                .data(OcrOutputMessage.DataField.builder()
                        .validationType(OcrOutputMessage.ValidationType.ai)
                        .validationStatus(OcrOutputMessage.ValidationStatus.PENDING)
                        .description("richiesta in corso")
                        .build())
                .build();
    }

    private OcrOutputMessage buildKoOcrMessage(OcrInputMessage ocrInputMessage) {
        return OcrOutputMessage.builder()
                .version(ocrInputMessage.getVersion())
                .commandId(ocrInputMessage.getCommandId())
                .commandType(ocrInputMessage.getCommandType())
                .data(OcrOutputMessage.DataField.builder()
                        .validationType(OcrOutputMessage.ValidationType.ai)
                        .validationStatus(OcrOutputMessage.ValidationStatus.KO)
                        .description("validazione fallita")
                        .build())
                .build();
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
