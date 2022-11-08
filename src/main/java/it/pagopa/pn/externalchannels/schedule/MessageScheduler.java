package it.pagopa.pn.externalchannels.schedule;

import it.pagopa.pn.externalchannels.dao.NotificationProgressDao;
import it.pagopa.pn.externalchannels.dto.CodeTimeToSend;
import it.pagopa.pn.externalchannels.dto.NotificationProgress;
import it.pagopa.pn.externalchannels.dto.safestorage.FileCreationResponseInt;
import it.pagopa.pn.externalchannels.dto.safestorage.FileCreationWithContentRequest;
import it.pagopa.pn.externalchannels.event.PnDeliveryPushEvent;
import it.pagopa.pn.externalchannels.middleware.DeliveryPushSendClient;
import it.pagopa.pn.externalchannels.model.SingleStatusUpdate;
import it.pagopa.pn.externalchannels.service.HistoricalRequestService;
import it.pagopa.pn.externalchannels.service.SafeStorageService;
import it.pagopa.pn.externalchannels.util.EventCodeInt;
import it.pagopa.pn.externalchannels.util.EventMessageUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.ByteArrayOutputStream;
import java.time.Instant;
import java.util.Collection;

@Component
@RequiredArgsConstructor
@Slf4j
public class MessageScheduler {

    public static final String LEGALFACTS_MEDIATYPE_XML = "application/xml";
    public static final String PN_NOTIFICATION_ATTACHMENTS = "PN_NOTIFICATION_ATTACHMENTS";
    public static final String SAVED = "SAVED";

    private final NotificationProgressDao dao;

    private final DeliveryPushSendClient deliveryPushSendClient;

    private final SafeStorageService safeStorageService;

    private final HistoricalRequestService historicalRequestService;


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
        CodeTimeToSend codeTimeToSend = notificationProgress.getCodeTimeToSendQueue().poll();
        log.debug("[{}] Processing codeTimeToSend: {}", notificationProgress.getIun(), codeTimeToSend);
        assert codeTimeToSend != null;
        String code = codeTimeToSend.getCode();
        String requestId = notificationProgress.getRequestId();
        String iun = notificationProgress.getIun();
        String channel = notificationProgress.getChannel();
        String destinationAddress = notificationProgress.getDestinationAddress();


        SingleStatusUpdate eventMessage = EventMessageUtil.buildMessageEvent(code, requestId, channel, destinationAddress);
        if (EventMessageUtil.LEGAL_CHANNELS.contains(channel)) {
            enrichWithLocation(eventMessage, iun);
        }
        PnDeliveryPushEvent pnDeliveryPushEvent = EventMessageUtil.buildDeliveryPushEvent(eventMessage, iun);
        log.info("[{}] Message to send: {}", iun, pnDeliveryPushEvent);
        deliveryPushSendClient.sendNotification(pnDeliveryPushEvent);
        log.debug("[{}] Message sent", iun);

        notificationProgress.setLastMessageSentTimestamp(Instant.now());
    }

    private void enrichWithLocation(SingleStatusUpdate eventMessage, String iun) {
        String code = eventMessage.getDigitalLegal().getEventCode().name();
        if (EventCodeInt.isWithAttachment(code)) {
            FileCreationWithContentRequest fileCreationRequest = new FileCreationWithContentRequest();
            fileCreationRequest.setContentType(LEGALFACTS_MEDIATYPE_XML);
            fileCreationRequest.setDocumentType(PN_NOTIFICATION_ATTACHMENTS);
            fileCreationRequest.setStatus(SAVED);
            fileCreationRequest.setContent(buildXml(eventMessage.getDigitalLegal().getRequestId(), code));
            log.info("[{}] Message sending to Safe Storage: {}", iun, fileCreationRequest);
            FileCreationResponseInt response = safeStorageService.createAndUploadContent(fileCreationRequest);
            log.info("[{}] Message sent to Safe Storage", iun);
            eventMessage.getDigitalLegal().getGeneratedMessage().setLocation("safestorage://" + response.getKey());
        }
    }

    private byte[] buildXml(String requestId, String code) {
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder;

        try {
            dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.newDocument();
            // add elements to Document
            Element rootElement = doc.createElement("Notifica");
            rootElement.setAttribute("status", EventCodeInt.getValueFromEnumString(code));
            rootElement.setAttribute("requestId", requestId);
            // append root element to document
            doc.appendChild(rootElement);


            // for output to bytearray-output
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            DOMSource source = new DOMSource(doc);
            ByteArrayOutputStream bos = new ByteArrayOutputStream();

            StreamResult result = new StreamResult(bos);
            transformer.transform(source, result);
            byte[] array = bos.toByteArray();
            log.info("XML creating:\n {}", new String(array));
            return array;
        } catch (Exception e) {
            throw new RuntimeException("Error generating XML", e);
        }
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
