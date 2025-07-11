package it.pagopa.pn.externalchannels.util;

import it.pagopa.pn.api.dto.events.EventPublisher;
import it.pagopa.pn.api.dto.events.StandardEventHeader;
import it.pagopa.pn.externalchannels.dao.EventCodeDocumentsDao;
import it.pagopa.pn.externalchannels.dto.*;
import it.pagopa.pn.externalchannels.dto.safestorage.FileCreationResponseInt;
import it.pagopa.pn.externalchannels.dto.safestorage.FileCreationWithContentRequest;
import it.pagopa.pn.externalchannels.event.OcrEvent;
import it.pagopa.pn.externalchannels.event.PaperChannelEvent;
import it.pagopa.pn.externalchannels.event.PnDeliveryPushEvent;
import it.pagopa.pn.externalchannels.exception.ExternalChannelsMockException;
import it.pagopa.pn.externalchannels.model.*;
import it.pagopa.pn.externalchannels.service.SafeStorageService;
import lombok.extern.slf4j.Slf4j;
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
import java.io.File;
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
import static it.pagopa.pn.externalchannels.util.ArchivesUtil.*;

@Slf4j
public class EventMessageUtil {

    private EventMessageUtil() {}

    public static final String LEGALFACTS_MEDIATYPE_XML = "application/xml";
    public static final String PN_EXTERNAL_LEGAL_FACTS = "PN_EXTERNAL_LEGAL_FACTS";
    public static final String CON020_DOCUMENT_TYPE = "Affido conservato";
    public static final String SAVED = "SAVED";
    public static final String MOCK_PREFIX = "mock-";
    private static final List<String> ERROR_CODES = List.of("C004", "C008", "C009", "C010");
    public static final List<String> LEGAL_CHANNELS = List.of("PEC", "REM");
    public static final String AR = "AR";
    public static final String _890 = "890";

    public static final List<String> PAPER_CHANNELS = List.of(AR, _890, "RIS", "RS","RIR");

    private static final String OK_CODE = "C003";

    private enum ZipSuffix {
        Z1("#Z1","23L_PN_EXTERNAL_LEGAL_FACTS_domicilio.zip"),
        Z2("#Z2","AR_EXTERNAL_LEGAL_FACTS_domicilio.zip"),
        Z3("#Z3","ARCAD_PN_EXTERNAL_LEGAL_FACTS.zip"),
        Z4("#Z4","SP04_23LFD_281526382192_381526382193.zip"),
        Z5("#Z5","23IFD_ACFD697319802149_697319802149.zip");
        private final String suffix;
        private final String resources;

        ZipSuffix(String suffix, String resources){
            this.suffix = suffix;
            this.resources = resources;
        }

        public static Optional<ZipSuffix> valueIfEndWithZipSuffix(String documentType){
            for(ZipSuffix zipSuffix: ZipSuffix.values()){
                if(documentType.endsWith(zipSuffix.suffix))return Optional.of(zipSuffix);
            }
            return Optional.empty();
        }
    }
    private static final String ZIP = "ZIP";
    private static final String SEVEN_ZIP = "7ZIP";
    public static final String OCR_KO = "OCR_KO";
    public static final String OCR_PENDING = "OCR_PENDING";


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
        AtomicReference<Integer> pdfPages = new AtomicReference<>(1);



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

            Optional<AdditionalAction> pagesAction = codeTimeToSend.getAdditionalActions().stream().filter(x -> x.getAction() == AdditionalAction.ADDITIONAL_ACTIONS.PAGES).findFirst();
            pagesAction.ifPresent(x -> pdfPages.set(Integer.valueOf(x.getInfo())));
            log.info("found code with PAGES, using pages={}", pagesAction);
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

            OffsetDateTime statusDateTime = getStatusDateTime(codeTimeToSend, notificationProgress);

            return buildPaperMessage(code, notificationProgress.getIun(), requestId, channel, discoveredAddress, delay.get(), delaydoc.get(), failcause.get(),
                    notificationProgress, eventCodeDocumentsDao, safeStorageService, pdfPages.get(), statusDateTime);
        }

        return buildDigitalCourtesyMessage(code, requestId, delay.get());
    }

    private static OffsetDateTime getStatusDateTime(CodeTimeToSend codeTimeToSend,
                                                    NotificationProgress notificationProgress) {
        // Genera lo stesso timestamp per gli eventi che fanno parte delle triplette
        // StatusCode termina con A, B, C, D, E, F e il prodotto è AR
        String code = codeTimeToSend.getCode();
        boolean endsWithABCDEF = code.matches(".*[A-F]$");
        boolean isARChannel = AR.equals(notificationProgress.getChannel());
        boolean isAutoDatetimeDisabled = codeTimeToSend.getAdditionalActions() != null &&
                codeTimeToSend.getAdditionalActions().stream()
                    .anyMatch(c -> c.getAction().equals(AdditionalAction.ADDITIONAL_ACTIONS.NOAUTODATETIME));

        if (endsWithABCDEF && isARChannel && !isAutoDatetimeDisabled) {
            log.info("Setting businessStatusDatetime for event. Code: {}, Channel: {}, Previous datetime: {}",
                    code, notificationProgress.getChannel(), notificationProgress.getBusinessStatusDatetime());
            if (notificationProgress.getBusinessStatusDatetime() == null) {
                notificationProgress.setBusinessStatusDatetime(OffsetDateTime.now());
            }
            return notificationProgress.getBusinessStatusDatetime();
        }
        return OffsetDateTime.now();
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

    private static SingleStatusUpdate buildPaperMessage(String code, String iun, String requestId, String productType, DiscoveredAddress discoveredAddress, Duration delay, Duration delaydoc, String failureCause, NotificationProgress notificationProgress, EventCodeDocumentsDao eventCodeDocumentsDao, SafeStorageService safeStorageService, Integer pages, OffsetDateTime statusDateTime) {
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
                                .statusDateTime(statusDateTime.minus(delay))
                                .statusDescription("Mock status"))
                .eventTimestamp(OffsetDateTime.now());

        EventCodeMapKey eventCodeMapKey = EventCodeMapKey.builder()
                .iun(iun)
                .recipient(notificationProgress.getDestinationAddress())
                .code(singleStatusUpdate.getAnalogMail().getStatusCode()).build();
        enrichWithAttachmentDetail(singleStatusUpdate, iun, eventCodeMapKey, delaydoc, eventCodeDocumentsDao, notificationProgress, safeStorageService, pages);

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

    public static OcrEvent buildOcrEvent(OcrOutputMessage event) {
        return OcrEvent.builder()
                .header(StandardEventHeader.builder()
                        .iun(event.getCommandId())
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
                                                   SafeStorageService safeStorageService,
                                                   Integer pages) {
        Optional<List<String>> eventCodeList = eventCodeDocumentsDao.consumeByKey(eventCodeMapKey);
        log.info("Event code  {} result list {}",eventCodeMapKey,eventCodeList);
        if(eventCodeList.isPresent()) {
            int id = 1;
            for(String documentType: eventCodeList.get()){
                eventMessage.getAnalogMail().addAttachmentsItem(buildAttachment(iun, id++, documentType, delaydoc, notificationProgress, safeStorageService, pages));
            }
            eventCodeDocumentsDao.deleteIfEmpty(eventCodeMapKey);
        }
    }



    private static AttachmentDetails buildAttachment(String iun, int id, String documentType, Duration delaydoc, NotificationProgress notificationProgress, SafeStorageService safeStorageService, Integer pages) {
        try {
            final FileCreationWithContentRequest fileCreationRequest;
            Optional<ZipSuffix> zipSuffixOptional = ZipSuffix.valueIfEndWithZipSuffix(documentType);
            if(zipSuffixOptional.isPresent()){
                log.info("[{}] ZIP attachment found!", iun);
                ZipSuffix zipSuffix = zipSuffixOptional.get();
                documentType = documentType.replace(zipSuffix.suffix, "");
                fileCreationRequest = buildZIPAttachment(zipSuffix.resources);
            } else if (documentType.endsWith(SEVEN_ZIP)) {
                log.info("[{}] CON020 7ZIP for attachment found!", iun);
                documentType = documentType.replace(ZIP, CON020_DOCUMENT_TYPE);
                fileCreationRequest = buildCON0207ZIPAttachment(notificationProgress, pages);
            } else if (documentType.endsWith(ZIP)) {
                log.info("[{}] CON020 ZIP for attachment found!", iun);
                documentType = documentType.replace(ZIP, CON020_DOCUMENT_TYPE);
                fileCreationRequest = buildCON020ZIPAttachment(notificationProgress, pages);
            } else {
                fileCreationRequest = buildPDFAttachment();
            }

            log.info("[{}] Receipt message sending to Safe Storage: {}", iun, fileCreationRequest);
            FileCreationResponseInt response = safeStorageService.createAndUploadContent(notificationProgress, fileCreationRequest);
            log.info("[{}] Message sent to Safe Storage", iun);
            return new AttachmentDetails()
                    .uri(SAFE_STORAGE_URL_PREFIX + response.getKey())
                    .id(iun + "DOCMock_"+id)
                    .sha256(response.getSha256())
                    .documentType(documentType)
                    .date(OffsetDateTime.now().minus(delaydoc));
        } catch (Exception e) {
            log.error(String.format("Error in buildAttachment with iun: %s", iun), e);
            return null;
        }
    }

    private static FileCreationWithContentRequest buildPDFAttachment() throws IOException {

            ClassPathResource classPathResource = new ClassPathResource("test.pdf");
            FileCreationWithContentRequest fileCreationRequest = new FileCreationWithContentRequest();
            fileCreationRequest.setContentType("application/pdf");
            fileCreationRequest.setDocumentType(PN_EXTERNAL_LEGAL_FACTS);
            fileCreationRequest.setStatus(SAVED);
            fileCreationRequest.setContent(Files.readAllBytes(classPathResource.getFile().toPath()));
            return fileCreationRequest;

    }

    private static FileCreationWithContentRequest buildZIPAttachment(String resources) throws IOException {

        ClassPathResource classPathResource = new ClassPathResource(resources);
        FileCreationWithContentRequest fileCreationRequest = new FileCreationWithContentRequest();
        fileCreationRequest.setContentType("application/zip");
        fileCreationRequest.setDocumentType(PN_EXTERNAL_LEGAL_FACTS);
        fileCreationRequest.setStatus(SAVED);
        fileCreationRequest.setContent(Files.readAllBytes(classPathResource.getFile().toPath()));
        return fileCreationRequest;
    }

    private static FileCreationWithContentRequest buildCON020ZIPAttachment(NotificationProgress notificationProgress, Integer pages) {
        FileCreationWithContentRequest fileCreationRequest = new FileCreationWithContentRequest();
        File zipFileCompleted = null;
        File bolFile = null;
        try {
            bolFile = createBolFile(notificationProgress, pages);
            byte[] zipFile = createZip(pages, bolFile);
            zipFileCompleted = createZip(createP7mFile(zipFile));
            fileCreationRequest.setContentType("application/octet-stream");
            fileCreationRequest.setDocumentType(PN_EXTERNAL_LEGAL_FACTS);
            fileCreationRequest.setStatus(SAVED);
            fileCreationRequest.setContent(Files.readAllBytes(zipFileCompleted.toPath()));
        } catch (Exception e) {
            log.error("Error reading zip file", e);
            throw new ExternalChannelsMockException("Error reading zip file", e);
        } finally {
            deleteFile(zipFileCompleted);
            deleteFile(bolFile);
        }
        return fileCreationRequest;
    }

    private static FileCreationWithContentRequest buildCON0207ZIPAttachment(NotificationProgress notificationProgress, Integer pages) {
        FileCreationWithContentRequest fileCreationRequest = null;
        File bolFile = null;
        File outputFile = null;
        try {
            bolFile = createBolFile(notificationProgress, pages);
            byte[] zipFile = create7Zip(pages, bolFile);
            outputFile = create7Zip(createP7mFile(zipFile));
            fileCreationRequest = new FileCreationWithContentRequest();
            fileCreationRequest.setContentType("application/octet-stream");
            fileCreationRequest.setDocumentType(PN_EXTERNAL_LEGAL_FACTS);
            fileCreationRequest.setStatus(SAVED);
            fileCreationRequest.setContent(Files.readAllBytes(outputFile.toPath()));
        } catch (IOException e) {
            log.error("Error reading 7zip file", e);
            throw new ExternalChannelsMockException("Error reading 7zip file", e);
        } finally {
            deleteFile(outputFile);
            deleteFile(bolFile);
        }
        return fileCreationRequest;
    }

}
