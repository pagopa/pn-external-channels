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
    public static final String PN_LEGAL_FACTS = "PN_LEGAL_FACTS";
    public static final String SAVED = "SAVED";

    private final NotificationProgressDao dao;

    private final DeliveryPushSendClient deliveryPushSendClient;

    private final SafeStorageService safeStorageService;

    private final HistoricalRequestService historicalRequestService;


    @Scheduled(cron = "${job.cron-expression}")
    public void run() {
        log.debug("[{}] CLOCK!", Instant.now());
        Collection<NotificationProgress> all = dao.findAll();
        Instant now = Instant.now();
        for (NotificationProgress notificationProgress : all) {

            if (isTimeToSendMessage(now, notificationProgress)) {
                saveInCache(notificationProgress);
                sendMessage(notificationProgress);
                notificationProgress.setLastMessageSentTimestamp(Instant.now());

                if (notificationProgress.getCodeTimeToSendQueue().isEmpty()) {
                    dao.delete(notificationProgress.getIun());
                    log.info("Deleted message with requestId: {}", notificationProgress.getRequestId());
                }
            }
        }
    }

    private boolean isTimeToSendMessage(Instant now, NotificationProgress notificationProgress) {
        Instant messageTimestamp = notificationProgress.getLastMessageSentTimestamp() == null ? notificationProgress.getCreateMessageTimestamp()
                : notificationProgress.getLastMessageSentTimestamp();

        CodeTimeToSend codeTimeToSend = notificationProgress.getCodeTimeToSendQueue().peek();
        assert codeTimeToSend != null;
        long seconds = codeTimeToSend.getTime().getSeconds();
        Instant messageTimestampPlusSeconds = messageTimestamp.plusSeconds(seconds);

        return now.getEpochSecond() >= messageTimestampPlusSeconds.getEpochSecond();
    }

    private void sendMessage(NotificationProgress notificationProgress) {
        CodeTimeToSend codeTimeToSend = notificationProgress.getCodeTimeToSendQueue().poll();
        assert codeTimeToSend != null;
        String code = codeTimeToSend.getCode();
        String requestId = notificationProgress.getRequestId();
        String iun = notificationProgress.getIun();
        String channel = notificationProgress.getChannel();
        String destinationAddress = notificationProgress.getDestinationAddress();


        SingleStatusUpdate eventMessage = EventMessageUtil.buildMessageEvent(code, requestId, channel, destinationAddress);
        if (EventMessageUtil.LEGAL_CHANNELS.contains(channel)) {
            enrichWithLocation(eventMessage);
        }
        PnDeliveryPushEvent pnDeliveryPushEvent = EventMessageUtil.buildDeliveryPushEvent(eventMessage, iun, requestId);
        log.info("Message to send: {}", pnDeliveryPushEvent);
        deliveryPushSendClient.sendNotification(pnDeliveryPushEvent);

        notificationProgress.setLastMessageSentTimestamp(Instant.now());
    }

    private void enrichWithLocation(SingleStatusUpdate eventMessage) {
        String code = eventMessage.getDigitalLegal().getEventCode().name();
        if (EventCodeInt.isWithAttachment(code)) {
            FileCreationWithContentRequest fileCreationRequest = new FileCreationWithContentRequest();
            fileCreationRequest.setContentType(LEGALFACTS_MEDIATYPE_XML);
            fileCreationRequest.setDocumentType(PN_LEGAL_FACTS);
            fileCreationRequest.setStatus(SAVED);
            fileCreationRequest.setContent(buildXml(eventMessage.getDigitalLegal().getRequestId(), code));
            FileCreationResponseInt response = safeStorageService.createAndUploadContent(fileCreationRequest);

            eventMessage.getDigitalLegal().getGeneratedMessage().setLocation(response.getKey());
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

    private void saveInCache(NotificationProgress notificationProgress) {
        String iun = notificationProgress.getIun();
        String requestId = notificationProgress.getRequestId();
        CodeTimeToSend codeTimeToSend = notificationProgress.getCodeTimeToSendQueue().peek();
        assert codeTimeToSend != null;
        historicalRequestService.save(iun, requestId, codeTimeToSend.getCode());
    }
}
