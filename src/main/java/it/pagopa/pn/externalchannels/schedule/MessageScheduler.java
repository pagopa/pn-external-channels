package it.pagopa.pn.externalchannels.schedule;

import it.pagopa.pn.externalchannels.dao.EventCodeDocumentsDao;
import it.pagopa.pn.externalchannels.dao.NotificationProgressDao;
import it.pagopa.pn.externalchannels.dto.CodeTimeToSend;
import it.pagopa.pn.externalchannels.dto.EventCodeMapKey;
import it.pagopa.pn.externalchannels.dto.NotificationProgress;
import it.pagopa.pn.externalchannels.dto.safestorage.FileCreationResponseInt;
import it.pagopa.pn.externalchannels.dto.safestorage.FileCreationWithContentRequest;
import it.pagopa.pn.externalchannels.exception.ExternalChannelsMockException;
import it.pagopa.pn.externalchannels.middleware.ProducerHandler;
import it.pagopa.pn.externalchannels.model.AttachmentDetails;
import it.pagopa.pn.externalchannels.model.SingleStatusUpdate;
import it.pagopa.pn.externalchannels.service.HistoricalRequestService;
import it.pagopa.pn.externalchannels.service.SafeStorageService;
import it.pagopa.pn.externalchannels.util.EventCodeIntForDigital;
import it.pagopa.pn.externalchannels.util.EventMessageUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import static it.pagopa.pn.externalchannels.middleware.safestorage.PnSafeStorageClient.SAFE_STORAGE_URL_PREFIX;

@Component
@RequiredArgsConstructor
@Slf4j
public class MessageScheduler {

    public static final String LEGALFACTS_MEDIATYPE_XML = "application/xml";
    public static final String PN_EXTERNAL_LEGAL_FACTS = "PN_EXTERNAL_LEGAL_FACTS";
    public static final String SAVED = "SAVED";

    private final NotificationProgressDao dao;

    private final EventCodeDocumentsDao eventCodeDocumentsDao;

    private final ProducerHandler producerHandler;

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
        String iun = notificationProgress.getIun();
        String channel = notificationProgress.getChannel();

        SingleStatusUpdate eventMessage = EventMessageUtil.buildMessageEvent(notificationProgress);
        if (EventMessageUtil.LEGAL_CHANNELS.contains(channel)) {
            enrichWithLocation(eventMessage, iun);
        }
        else if(EventMessageUtil.PAPER_CHANNELS.contains(channel)) {
            EventCodeMapKey eventCodeMapKey = EventCodeMapKey.builder()
                    .iun(iun)
                    .recipient(notificationProgress.getDestinationAddress())
                    .code(eventMessage.getAnalogMail().getStatusCode()).build();
            enrichWithAttachmentDetail(eventMessage, iun, eventCodeMapKey);
        }

        producerHandler.sendToQueue(notificationProgress, eventMessage);

        notificationProgress.setLastMessageSentTimestamp(Instant.now());
    }

    private void enrichWithLocation(SingleStatusUpdate eventMessage, String iun) {
        String code = eventMessage.getDigitalLegal().getEventCode().name();
        if (EventCodeIntForDigital.isWithAttachment(code)) {
            FileCreationWithContentRequest fileCreationRequest = new FileCreationWithContentRequest();
            fileCreationRequest.setContentType(LEGALFACTS_MEDIATYPE_XML);
            fileCreationRequest.setDocumentType(PN_EXTERNAL_LEGAL_FACTS);
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
            rootElement.setAttribute("status", EventCodeIntForDigital.getValueFromEnumString(code));
            rootElement.setAttribute("requestId", requestId);
            // append root element to document
            doc.appendChild(rootElement);


            // for output to bytearray-output
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            // to be compliant, prohibit the use of all protocols by external entities:
            transformerFactory.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
            transformerFactory.setAttribute(XMLConstants.ACCESS_EXTERNAL_STYLESHEET, "");
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
            throw new ExternalChannelsMockException("Error generating XML", e);
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

    private void enrichWithAttachmentDetail(SingleStatusUpdate eventMessage, String iun, EventCodeMapKey eventCodeMapKey) {
        Optional<List<String>> eventCodeList = eventCodeDocumentsDao.consumeByKey(eventCodeMapKey);
        if(eventCodeList.isPresent()) {
            int id = 1;
            for(String documentType: eventCodeList.get()){
                eventMessage.getAnalogMail().addAttachmentsItem(buildAttachment(iun, id++, documentType));
            }
            eventCodeDocumentsDao.deleteIfEmpty(eventCodeMapKey);
        }
    }

    private AttachmentDetails buildAttachment(String iun,int id, String documentType) {
        try {
            ClassPathResource classPathResource = new ClassPathResource("test.pdf");
            FileCreationWithContentRequest fileCreationRequest = new FileCreationWithContentRequest();
            fileCreationRequest.setContentType("application/pdf");
            fileCreationRequest.setDocumentType(PN_EXTERNAL_LEGAL_FACTS);
            fileCreationRequest.setStatus(SAVED);
            fileCreationRequest.setContent(Files.readAllBytes(classPathResource.getFile().toPath()));
            log.info("[{}] Receipt message sending to Safe Storage: {}", iun, fileCreationRequest);
            FileCreationResponseInt response = safeStorageService.createAndUploadContent(fileCreationRequest);
            log.info("[{}] Message sent to Safe Storage", iun);
            return new AttachmentDetails()
                    .url(SAFE_STORAGE_URL_PREFIX + response.getKey())
                    .id(iun + "DOCMock_"+id)
                    .documentType(documentType)
                    .date(OffsetDateTime.now());
        } catch (IOException e) {
            log.error(String.format("Error in buildAttachment with iun: %s", iun), e);
            return null;
        }
    }

}
