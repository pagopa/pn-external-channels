package it.pagopa.pn.externalchannels.util;

import it.pagopa.pn.api.dto.events.EventPublisher;
import it.pagopa.pn.api.dto.events.StandardEventHeader;
import it.pagopa.pn.externalchannels.dao.EventCodeDocumentsDao;
import it.pagopa.pn.externalchannels.dto.AdditionalAction;
import it.pagopa.pn.externalchannels.dto.CodeTimeToSend;
import it.pagopa.pn.externalchannels.dto.EventCodeMapKey;
import it.pagopa.pn.externalchannels.dto.NotificationProgress;
import it.pagopa.pn.externalchannels.dto.safestorage.FileCreationResponseInt;
import it.pagopa.pn.externalchannels.dto.safestorage.FileCreationWithContentRequest;
import it.pagopa.pn.externalchannels.event.PaperChannelEvent;
import it.pagopa.pn.externalchannels.event.PnDeliveryPushEvent;
import it.pagopa.pn.externalchannels.exception.ExternalChannelsMockException;
import it.pagopa.pn.externalchannels.model.*;
import it.pagopa.pn.externalchannels.service.SafeStorageService;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.weaver.ast.Not;
import org.springframework.core.io.ClassPathResource;
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
import java.time.Duration;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

import static it.pagopa.pn.externalchannels.middleware.safestorage.PnSafeStorageClient.SAFE_STORAGE_URL_PREFIX;

@Slf4j
public class EventMessageUtil {

    private EventMessageUtil() {}

    public static final String LEGALFACTS_MEDIATYPE_XML = "application/xml";
    public static final String PN_EXTERNAL_LEGAL_FACTS = "PN_EXTERNAL_LEGAL_FACTS";
    public static final String SAVED = "SAVED";

    public static final String MOCK_PREFIX = "mock-";

    private static final List<String> ERROR_CODES = List.of("C004", "C008", "C009", "C010");
    public static final List<String> LEGAL_CHANNELS = List.of("PEC", "REM");
    public static final String AR = "AR";
    public static final String _890 = "890";

    public static final List<String> PAPER_CHANNELS = List.of(AR, _890, "RIS", "RS","RIR");

    private static final String OK_CODE = "C003";

    public static SingleStatusUpdate buildMessageEvent(NotificationProgress notificationProgress, SafeStorageService safeStorageService, EventCodeDocumentsDao eventCodeDocumentsDao) {
        LinkedList<CodeTimeToSend> codeTimeToSends = new LinkedList<>(notificationProgress.getCodeTimeToSendQueue());
        CodeTimeToSend codeTimeToSend = codeTimeToSends.poll();
        log.debug("[{}] Processing codeTimeToSend: {}", notificationProgress.getIun(), codeTimeToSend);
        assert codeTimeToSend != null;
        notificationProgress.setCodeTimeToSendQueue(codeTimeToSends);

        String code = codeTimeToSend.getCode();
        String requestId = notificationProgress.getRequestId();
        String channel = notificationProgress.getChannel();
        DiscoveredAddress discoveredAddress = null;
        AtomicReference<Duration> delay = new AtomicReference<>(Duration.ZERO);
        AtomicReference<Duration> delaydoc = new AtomicReference<>(Duration.ZERO);
        AtomicReference<String> failcause = new AtomicReference<>(null);


        if (codeTimeToSend.getAdditionalActions() != null) {
            Optional<AdditionalAction> additionalAction = codeTimeToSend.getAdditionalActions().stream().filter(x -> x.getAction() == AdditionalAction.ADDITIONAL_ACTIONS.DELAY).findFirst();
            additionalAction.ifPresent(x -> delay.set(org.springframework.boot.convert.DurationStyle.detectAndParse(x.getInfo().replace("+","-"))));
            log.info("found code with DELAY, using delay={}", delay);
            delaydoc.set(delay.get());

            Optional<AdditionalAction> additionalActionDOC = codeTimeToSend.getAdditionalActions().stream().filter(x -> x.getAction() == AdditionalAction.ADDITIONAL_ACTIONS.DELAYDOC).findFirst();
            additionalActionDOC.ifPresent(x -> delaydoc.set(org.springframework.boot.convert.DurationStyle.detectAndParse(x.getInfo().replace("+","-"))));
            log.info("found code with DELAYDOC, using delay={}", delaydoc);


            Optional<AdditionalAction> failCauseAction = codeTimeToSend.getAdditionalActions().stream().filter(x -> x.getAction() == AdditionalAction.ADDITIONAL_ACTIONS.FAILCAUSE).findFirst();
            failCauseAction.ifPresent(x -> failcause.set(x.getInfo()));
            log.info("found code with FAILCAUSE, using fail={}", failcause);
        }



        if (LEGAL_CHANNELS.contains(channel)) {
            return buildLegalMessage(code, requestId, delay.get(), notificationProgress.getIun(), safeStorageService);
        } else if (PAPER_CHANNELS.contains(channel)) {

            if (codeTimeToSend.getAdditionalActions() != null
                    && codeTimeToSend.getAdditionalActions().stream().anyMatch(x -> x.getAction() == AdditionalAction.ADDITIONAL_ACTIONS.DISCOVERY)
                    &&  notificationProgress.getDiscoveredAddress() != null) {
                Optional<AdditionalAction> optionalAdditionalAction = codeTimeToSend.getAdditionalActions().stream().filter(x -> x.getAction() == AdditionalAction.ADDITIONAL_ACTIONS.DISCOVERY).findFirst();
                if (optionalAdditionalAction.isPresent()) {
                    discoveredAddress = notificationProgress.getDiscoveredAddress();
                    if (optionalAdditionalAction.get().getInfo() != null)
                        discoveredAddress.setCap(optionalAdditionalAction.get().getInfo());
                    log.info("found code with discovery enabled, using discovered={} and CAP={}", discoveredAddress.getAddress(), discoveredAddress.getCap());
                }
            }

            return buildPaperMessage(code, notificationProgress.getIun(), requestId, channel, discoveredAddress, delay.get(), delaydoc.get(), failcause.get(),
                    notificationProgress, eventCodeDocumentsDao, safeStorageService);
        }

        return buildDigitalCourtesyMessage(code, requestId, delay.get());
    }

    private static SingleStatusUpdate buildLegalMessage(String code, String requestId, Duration delay, String iun, SafeStorageService safeStorageService) {

        SingleStatusUpdate singleStatusUpdate = new SingleStatusUpdate()
                .digitalLegal(
                        new LegalMessageSentDetails()
                                .eventCode(LegalMessageSentDetails.EventCodeEnum.fromValue(code))
                                .requestId(requestId)
                                .status(buildStatus(code))
                                .eventTimestamp(OffsetDateTime.now().minus(delay))
                                .generatedMessage(new DigitalMessageReference().system("mock-system").id(MOCK_PREFIX + UUID.randomUUID()))
                )
                .eventTimestamp(OffsetDateTime.now());

        enrichWithLocation(singleStatusUpdate, iun, null, safeStorageService);
        return singleStatusUpdate;
    }


    private static void enrichWithLocation(SingleStatusUpdate eventMessage, String iun, NotificationProgress notificationProgress, SafeStorageService safeStorageService) {
        String code = eventMessage.getDigitalLegal().getEventCode().name();
        if (EventCodeIntForDigital.isWithAttachment(code)) {
            FileCreationWithContentRequest fileCreationRequest = new FileCreationWithContentRequest();
            fileCreationRequest.setContentType(LEGALFACTS_MEDIATYPE_XML);
            fileCreationRequest.setDocumentType(PN_EXTERNAL_LEGAL_FACTS);
            fileCreationRequest.setStatus(SAVED);
            fileCreationRequest.setContent(buildXml(eventMessage.getDigitalLegal().getRequestId(), code));
            log.info("[{}] Message sending to Safe Storage: {}", iun, fileCreationRequest);
            FileCreationResponseInt response = safeStorageService.createAndUploadContent(notificationProgress, fileCreationRequest);
            log.info("[{}] Message sent to Safe Storage", iun);
            eventMessage.getDigitalLegal().getGeneratedMessage().setLocation("safestorage://" + response.getKey());
        }
    }

    private static byte[] buildXml(String requestId, String code) {
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

    private static SingleStatusUpdate buildDigitalCourtesyMessage(String code, String requestId, Duration delay) {

        return new SingleStatusUpdate()
                .digitalCourtesy(
                        new CourtesyMessageProgressEvent()
                                .eventCode(CourtesyMessageProgressEvent.EventCodeEnum.fromValue(code))
                                .requestId(requestId)
                                .status(buildStatus(code))
                                .eventTimestamp(OffsetDateTime.now().minus(delay))
                                .generatedMessage(new DigitalMessageReference().system("mock-system").id(MOCK_PREFIX + UUID.randomUUID()))
                )
                .eventTimestamp(OffsetDateTime.now());
    }

    private static SingleStatusUpdate buildPaperMessage(String code, String iun, String requestId, String productType, DiscoveredAddress discoveredAddress, Duration delay, Duration delaydoc, String failureCause, NotificationProgress notificationProgress, EventCodeDocumentsDao eventCodeDocumentsDao, SafeStorageService safeStorageService) {
        SingleStatusUpdate singleStatusUpdate = new SingleStatusUpdate()
                .analogMail(
                        new PaperProgressStatusEvent()
                                .iun(iun)
                                .registeredLetterCode(notificationProgress.getRegisteredLetterCode())
                                .discoveredAddress(discoveredAddress)
                                .requestId(requestId)
                                .productType(productType)
                                .clientRequestTimeStamp(OffsetDateTime.now())
                                .attachments(null)
                                .deliveryFailureCause(failureCause)
                                .statusCode(code)
                                .statusDateTime(OffsetDateTime.now().minus(delay))
                                .statusDescription("Mock status"))
                .eventTimestamp(OffsetDateTime.now());

        EventCodeMapKey eventCodeMapKey = EventCodeMapKey.builder()
                .iun(iun)
                .recipient(notificationProgress.getDestinationAddress())
                .code(singleStatusUpdate.getAnalogMail().getStatusCode()).build();
        enrichWithAttachmentDetail(singleStatusUpdate, iun, eventCodeMapKey, delaydoc, eventCodeDocumentsDao, notificationProgress, safeStorageService);

        return singleStatusUpdate;
    }


    private static ProgressEventCategory buildStatus(String code) {
        if (code.equals(OK_CODE)) {
            return ProgressEventCategory.OK;
        }
        if (ERROR_CODES.contains(code)) {
            return ProgressEventCategory.ERROR;
        }
        return ProgressEventCategory.PROGRESS;
    }

    public static PnDeliveryPushEvent buildDeliveryPushEvent(SingleStatusUpdate event, String iun) {
        return PnDeliveryPushEvent.builder()
                .header(StandardEventHeader.builder()
                        .iun(iun)
                        .eventId(MOCK_PREFIX + UUID.randomUUID())
                        .eventType("EXTERNAL_CHANNELS_EVENT")
                        .publisher(EventPublisher.EXTERNAL_CHANNELS.name())
                        .createdAt(Instant.now())
                        .build())
                .payload(event)
                .build();
    }

    public static PaperChannelEvent buildPaperChannelEvent(SingleStatusUpdate event, String iun) {
        return PaperChannelEvent.builder()
                .header(StandardEventHeader.builder()
                        .iun(iun)
                        .eventId(MOCK_PREFIX + UUID.randomUUID())
                        .eventType("EXTERNAL_CHANNELS_EVENT")
                        .publisher(EventPublisher.EXTERNAL_CHANNELS.name())
                        .createdAt(Instant.now())
                        .build())
                .payload(event)
                .build();
    }



    private static void enrichWithAttachmentDetail(SingleStatusUpdate eventMessage, String iun,
                                                   EventCodeMapKey eventCodeMapKey, Duration delaydoc,
                                                   EventCodeDocumentsDao eventCodeDocumentsDao,
                                                   NotificationProgress notificationProgress,
                                                   SafeStorageService safeStorageService) {
        Optional<List<String>> eventCodeList = eventCodeDocumentsDao.consumeByKey(eventCodeMapKey);
        log.info("Event code  {} result list {}",eventCodeMapKey,eventCodeList);
        if(eventCodeList.isPresent()) {
            int id = 1;
            for(String documentType: eventCodeList.get()){
                eventMessage.getAnalogMail().addAttachmentsItem(buildAttachment(iun, id++, documentType, delaydoc, notificationProgress, safeStorageService));
            }
            eventCodeDocumentsDao.deleteIfEmpty(eventCodeMapKey);
        }
    }



    private static AttachmentDetails buildAttachment(String iun, int id, String documentType, Duration delaydoc, NotificationProgress notificationProgress, SafeStorageService safeStorageService) {
        try {
            ClassPathResource classPathResource = new ClassPathResource("test.pdf");
            FileCreationWithContentRequest fileCreationRequest = new FileCreationWithContentRequest();
            fileCreationRequest.setContentType("application/pdf");
            fileCreationRequest.setDocumentType(PN_EXTERNAL_LEGAL_FACTS);
            fileCreationRequest.setStatus(SAVED);
            fileCreationRequest.setContent(Files.readAllBytes(classPathResource.getFile().toPath()));
            log.info("[{}] Receipt message sending to Safe Storage: {}", iun, fileCreationRequest);
            FileCreationResponseInt response = safeStorageService.createAndUploadContent(notificationProgress, fileCreationRequest);
            log.info("[{}] Message sent to Safe Storage", iun);
            return new AttachmentDetails()
                    .uri(SAFE_STORAGE_URL_PREFIX + response.getKey())
                    .id(iun + "DOCMock_"+id)
                    .sha256(response.getSha256())
                    .documentType(documentType)
                    .date(OffsetDateTime.now().minus(delaydoc));
        } catch (IOException e) {
            log.error(String.format("Error in buildAttachment with iun: %s", iun), e);
            return null;
        }
    }


}
