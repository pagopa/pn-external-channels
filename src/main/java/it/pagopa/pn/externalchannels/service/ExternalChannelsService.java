package it.pagopa.pn.externalchannels.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import it.pagopa.pn.externalchannels.config.PnExternalChannelsProperties;
import it.pagopa.pn.externalchannels.config.aws.EventCodeSequenceDTO;
import it.pagopa.pn.externalchannels.config.aws.EventCodeSequenceParameterConsumer;
import it.pagopa.pn.externalchannels.config.aws.ServiceIdEndpointDTO;
import it.pagopa.pn.externalchannels.config.aws.ServiceIdEndpointParameterConsumer;
import it.pagopa.pn.externalchannels.dao.*;
import it.pagopa.pn.externalchannels.dto.AdditionalAction;
import it.pagopa.pn.externalchannels.dto.CodeTimeToSend;
import it.pagopa.pn.externalchannels.dto.DiscoveredAddressEntity;
import it.pagopa.pn.externalchannels.dto.NotificationProgress;
import it.pagopa.pn.externalchannels.mapper.RequestsToReceivedMessagesMapper;
import it.pagopa.pn.externalchannels.mapper.SmartMapper;
import it.pagopa.pn.externalchannels.middleware.InternalSendClient;
import it.pagopa.pn.externalchannels.model.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

import static it.pagopa.pn.externalchannels.dto.NotificationProgress.PROGRESS_OUTPUT_CHANNEL.QUEUE_USER_ATTRIBUTES;

@Service
@RequiredArgsConstructor
@Slf4j
public class ExternalChannelsService {

    private static final String IUN_ALREADY_EXISTS_MESSAGE = "[%s] Iun already inserted!";

    private static final List<String> OK_REQUEST_CODE_DIGITAL = List.of("C000", "C001", "C005", "C003");

    private static final List<String> FAIL_REQUEST_CODE_DIGITAL = List.of("C001", "C007", "C004");

    private static final List<String> OK_REQUEST_CODE_MAIL = List.of("M003");

    private static final List<String> FAIL_REQUEST_CODE_MAIL = List.of("M008");

    private static final List<String> OK_REQUEST_CODE_SMS = List.of("S003");

    private static final List<String> FAIL_REQUEST_CODE_SMS = List.of("S008");

    private static final List<String> OK_REQUEST_CODE_PAPER = List.of("CON080", "RECRN001A", "RECRN001B", "RECRN001C");

    private static final List<String> FAIL_REQUEST_CODE_PAPER = List.of("CON080", "RECRN002A", "RECRN002B", "RECRN002C");

    // ora l'indirizzo può arrivare in maiuscolo
    private static final String SEQUENCE_REGEXP = "(?i).*@sequence\\.";

    private static final String DISCOVERED_MARKER = "@discovered";

    private final EventCodeSequenceParameterConsumer eventCodeSequenceParameterConsumer;
    private final ServiceIdEndpointParameterConsumer serviceIdEndpointParameterConsumer;

    private static final List<String> SEQUENCE_PARAMETER_NAME = Arrays.asList(
            "MapExternalChannelMockSequence",
            "MapExternalChannelMockSequence2",
            "MapExternalChannelMockSequence3");
    private static final String SERVICEID_PARAMETER_NAME = "MapExternalChannelMockServiceIdEndpoint";

    private final NotificationProgressDao notificationProgressDao;

    private final EventCodeDocumentsDao eventCodeDocumentsDao;

    private final PnExternalChannelsProperties pnExternalChannelsProperties;
    
    private final VerificationCodeService verificationCodeService;

    private final InternalSendClient internalSendClient;

    private final ReceivedMessageEntityDaoDynamo receivedMessageEntityDaoDynamo;

    public void sendDigitalLegalMessage(DigitalNotificationRequest digitalNotificationRequest, String appSourceName) {
        NotificationProgress.PROGRESS_OUTPUT_CHANNEL outputChannel = getOutputQueueFromSource(appSourceName);
        if(QUEUE_USER_ATTRIBUTES.equals(outputChannel)){
            verificationCodeService.saveVerificationCode(digitalNotificationRequest.getEventType(), digitalNotificationRequest.getMessageText(), digitalNotificationRequest.getReceiverDigitalAddress());
        }
        
        NotificationProgress notificationProgress = buildNotificationProgress(digitalNotificationRequest.getRequestId(),
                digitalNotificationRequest.getReceiverDigitalAddress(), outputChannel,
                null,null,null,
                digitalNotificationRequest.getChannel().name(), FAIL_REQUEST_CODE_DIGITAL, OK_REQUEST_CODE_DIGITAL,
                selectSequenceInParameter(digitalNotificationRequest.getReceiverDigitalAddress(),digitalNotificationRequest.getChannel().getValue(),SEQUENCE_PARAMETER_NAME,getOutputQueueFromSource(appSourceName)));

        boolean inserted = notificationProgressDao.insert(notificationProgress);

        if (! inserted) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, String.format(IUN_ALREADY_EXISTS_MESSAGE, digitalNotificationRequest.getRequestId()));
        }
        else {
            receivedMessageEntityDaoDynamo.put(RequestsToReceivedMessagesMapper.map(digitalNotificationRequest));
        }

        internalSendClient.sendNotification(notificationProgress);
    }

    public void sendDigitalCourtesyMessage(DigitalCourtesyMailRequest digitalCourtesyMailRequest, String appSourceName) {
        NotificationProgress.PROGRESS_OUTPUT_CHANNEL outputChannel = getOutputQueueFromSource(appSourceName);
        log.info("OutputChannel is {}",outputChannel);
        if(QUEUE_USER_ATTRIBUTES.equals(outputChannel)){
            log.info("start saveVerificationCode");
            try {
                // nel caso di messaggi che hanno "pec-rejected" nell'id, non mi interessa recuperare il codice di verifica.
                if (!digitalCourtesyMailRequest.getRequestId().contains("pec-rejected"))
                    verificationCodeService.saveVerificationCode(digitalCourtesyMailRequest.getEventType(), digitalCourtesyMailRequest.getMessageText(), digitalCourtesyMailRequest.getReceiverDigitalAddress());
                else
                    log.info("skip saveVerificationCode for pec-rejected");
            } catch (Exception e) {
                log.warn("Cannot detect verification code", e);
            }
        }

        NotificationProgress notificationProgress = buildNotificationProgress(digitalCourtesyMailRequest.getRequestId(),
                digitalCourtesyMailRequest.getReceiverDigitalAddress(), getOutputQueueFromSource(appSourceName),
                null,null,null,
                digitalCourtesyMailRequest.getChannel().name(), FAIL_REQUEST_CODE_MAIL, OK_REQUEST_CODE_MAIL, Optional.empty());

        boolean inserted = notificationProgressDao.insert(notificationProgress);

        if (! inserted) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, String.format(IUN_ALREADY_EXISTS_MESSAGE, digitalCourtesyMailRequest.getRequestId()));
        }
        else {
            receivedMessageEntityDaoDynamo.put(RequestsToReceivedMessagesMapper.map(digitalCourtesyMailRequest));
        }

        internalSendClient.sendNotification(notificationProgress);
    }

    public void sendCourtesyShortMessage(DigitalCourtesySmsRequest digitalCourtesySmsRequest, String appSourceName) {

        NotificationProgress notificationProgress = buildNotificationProgress(digitalCourtesySmsRequest.getRequestId(),
                digitalCourtesySmsRequest.getReceiverDigitalAddress(), getOutputQueueFromSource(appSourceName),
                null,null,null,
                digitalCourtesySmsRequest.getChannel().name(), FAIL_REQUEST_CODE_SMS, OK_REQUEST_CODE_SMS,Optional.empty());

        boolean inserted = notificationProgressDao.insert(notificationProgress);

        if (! inserted) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, String.format(IUN_ALREADY_EXISTS_MESSAGE, digitalCourtesySmsRequest.getRequestId()));
        }
        else {
            receivedMessageEntityDaoDynamo.put(RequestsToReceivedMessagesMapper.map(digitalCourtesySmsRequest));
        }

        internalSendClient.sendNotification(notificationProgress);
    }

    public void sendPaperEngageRequest(PaperEngageRequest paperEngageRequest, String appSource) {
        String address = paperEngageRequest.getReceiverAddress();
        // Per il cartaceo, l'endpoint finale dipende dalla configurazione
        // per semplicità degli step successivi, la salvo direttamente nel notification progress
        AtomicReference<NotificationProgress.PROGRESS_OUTPUT_CHANNEL> output = new AtomicReference<>(NotificationProgress.PROGRESS_OUTPUT_CHANNEL.QUEUE_PAPER_CHANNEL);
        AtomicReference<String> outputEndpoint = new AtomicReference<>();
        AtomicReference<String> outputServiceId = new AtomicReference<>();
        AtomicReference<String> outputApiKey = new AtomicReference<>();
        if (appSource != null) {
            Optional<ServiceIdEndpointDTO[]> sequenceEventCode = serviceIdEndpointParameterConsumer.getParameterValue(SERVICEID_PARAMETER_NAME, ServiceIdEndpointDTO[].class);
            sequenceEventCode.ifPresent(sequenceEventCodes -> {
                Optional<ServiceIdEndpointDTO> optres = Arrays.stream(sequenceEventCode.get()).filter(x -> x.serviceId().equals(appSource)).findFirst();
                optres.ifPresent(serviceIdEndpointDTO -> {
                    output.set(NotificationProgress.PROGRESS_OUTPUT_CHANNEL.WEBHOOK_EXT_CHANNEL);
                    outputEndpoint.set(serviceIdEndpointDTO.endpoint());
                    outputServiceId.set(serviceIdEndpointDTO.endpointServiceId());
                    try {
                        Optional<PnExternalChannelsProperties.WebhookApikeys> res = pnExternalChannelsProperties.findExtchannelwebhookApiKey(appSource);
                        res.ifPresent(webhookApikeys -> outputApiKey.set(webhookApikeys.getApiKey()));

                    } catch (JsonProcessingException e) {
                        log.error("cannot parse apikeys", e);
                    }
                });
            });
        }


        NotificationProgress notificationProgress = buildNotificationProgress(paperEngageRequest.getRequestId(),
                address, output.get(), outputEndpoint.get(), outputServiceId.get(), outputApiKey.get(), paperEngageRequest.getProductType(), FAIL_REQUEST_CODE_PAPER, OK_REQUEST_CODE_PAPER,
                selectSequenceInParameter(address,paperEngageRequest.getProductType(),SEQUENCE_PARAMETER_NAME,getOutputQueueFromSource(appSource)));

        boolean inserted = notificationProgressDao.insert(notificationProgress);

        if (! inserted) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, String.format(IUN_ALREADY_EXISTS_MESSAGE, paperEngageRequest.getRequestId()));
        }
        else {
            receivedMessageEntityDaoDynamo.put(RequestsToReceivedMessagesMapper.map(paperEngageRequest));
        }

        internalSendClient.sendNotification(notificationProgress);
    }



    private NotificationProgress buildNotificationProgress(String requestId, String receiverDigitalAddress,
                                                           NotificationProgress.PROGRESS_OUTPUT_CHANNEL output,
                                                           String outputEndpoint, String outputServiceId, String outputApikey,
                                                           String channel, List<String> failRequests, List<String> okRequests,Optional<String> requestSearched) {
        NotificationProgress notificationProgress;
        String iun = requestId;
        NotificationProgress.PROGRESS_OUTPUT_CHANNEL userAttributesChannel = NotificationProgress.PROGRESS_OUTPUT_CHANNEL.QUEUE_USER_ATTRIBUTES;
        if (requestId.contains(".")) {
            iun = requestId.split("\\.")[1];
            iun = iun.contains("IUN_") ? iun.substring(iun.indexOf("IUN_") + 4) : iun;
        }

        if(requestSearched.isPresent() && (output != userAttributesChannel || (receiverDigitalAddress.toUpperCase(Locale.ROOT).contains("PEC-MOCK"))) ){
            notificationProgress = buildNotificationCustomized(requestSearched.get(), iun, requestId,receiverDigitalAddress);
        }else if (receiverDigitalAddress.toLowerCase(Locale.ROOT).contains("@fail") && (output != userAttributesChannel || (receiverDigitalAddress.toLowerCase(Locale.ROOT).contains("@failalways")))
                || receiverDigitalAddress.replaceFirst("\\+39", "").startsWith("001")) {
            notificationProgress = buildNotification(failRequests);
            if(receiverDigitalAddress.toLowerCase(Locale.ROOT).contains("discovered")) {
                notificationProgress.setDiscoveredAddress(buildMockDiscoveredAddress(""));
            }
        } else if (receiverDigitalAddress.toLowerCase(Locale.ROOT).contains("@sequence")  && (output != userAttributesChannel)) { //si presuppone che per gli sms non ci sia il caso sequence
            notificationProgress = buildNotificationCustomized(receiverDigitalAddress, iun, requestId,receiverDigitalAddress);
        } else {
            notificationProgress = buildNotification(okRequests);
        }

        notificationProgress.setRequestId(requestId);
        notificationProgress.setDestinationAddress(receiverDigitalAddress);
        notificationProgress.setCreateMessageTimestamp(Instant.now());
        notificationProgress.setOutput(output);
        notificationProgress.setIun(iun);
        notificationProgress.setChannel(channel);
        notificationProgress.setOutputEndpoint(outputEndpoint);
        notificationProgress.setOutputServiceId(outputServiceId);
        notificationProgress.setOutputApiKey(outputApikey);
        notificationProgress.setRegisteredLetterCode(UUID.randomUUID().toString().replace("-",""));

        return notificationProgress;

    }

    private NotificationProgress buildNotification(List<String> codeToSendList) {
        NotificationProgress notificationProgress = new NotificationProgress();
        List<CodeTimeToSend> codeTimeToSends = codeToSendList.stream().map(codeToSend -> new CodeTimeToSend(codeToSend,
                Duration.ofSeconds(5), null)).toList();
        notificationProgress.setCodeTimeToSendQueue(new LinkedList<>(codeTimeToSends));


        return notificationProgress;
    }

    private NotificationProgress buildNotificationCustomized(String receiverDigitalAddress, String iun, String requestId,String addressAlias) {
        NotificationProgress notificationProgress = new NotificationProgress();
        notificationProgress.setCodeTimeToSendQueue(new LinkedList<>());

        String receiverClean = receiverDigitalAddress
                .replaceFirst(SEQUENCE_REGEXP, "");

        // per supportare le sequence, ora che è stata aggiunta una regexp stringente, tolgo l'eventuale .it finale
        if (receiverClean.toLowerCase(Locale.ROOT).endsWith(".it"))
            receiverClean = receiverClean.substring(0, receiverClean.length()-3);

        if(receiverClean.toLowerCase(Locale.ROOT).contains("pcretry")) {
            receiverClean = getSequenceOfPcRetry(receiverClean,requestId);
        }
        if (receiverClean.toLowerCase(Locale.ROOT).contains("attempt")) {
            receiverClean = getSequenceOfMacroAttempts(receiverClean, requestId);
        }
        if (receiverClean.contains("_")) {
            receiverClean = getSequenceOfMicroAttempts(receiverClean, iun, receiverDigitalAddress);
        }
        if(receiverClean.toLowerCase(Locale.ROOT).contains("retry")) {
            receiverClean = getSequenceOfRetry(receiverClean,requestId);
        }

        if(receiverClean.contains(DISCOVERED_MARKER)) {
            String discoveredSequence = receiverClean.substring(receiverClean.indexOf(DISCOVERED_MARKER));
            discoveredSequence = discoveredSequence.replace(DISCOVERED_MARKER, "@sequence").replace(DISCOVERED_MARKER.toUpperCase(Locale.ROOT), "@sequence");

            notificationProgress.setDiscoveredAddress(buildMockDiscoveredAddress(discoveredSequence));
            log.info("discovered address will be address={}", notificationProgress.getDiscoveredAddress().getAddress());
            
            receiverClean = receiverClean.substring(0, receiverClean.indexOf(DISCOVERED_MARKER));
        }

        String[] timeCodeCoupleArray = receiverClean.split("\\.");

        for (String timeCodeCouple : timeCodeCoupleArray) {
            String[] timeCodeCoupleSplit = timeCodeCouple.split("-");
            String time = "PT" + timeCodeCoupleSplit[0];
            String code = timeCodeCoupleSplit[1];

            List<AdditionalAction> additionalActions = null;
            if(code.contains("[")) {
                additionalActions = getAdditionalActionsFromCode(code);
                List<String> documentList = additionalActions.stream().filter(x -> x.getAction() == AdditionalAction.ADDITIONAL_ACTIONS.DOC)
                        .map(AdditionalAction::getInfo)
                        .map(this::formatDocumentCode).toList();

                code = code.substring(0,code.indexOf("["));
                if (!documentList.isEmpty())
                    eventCodeDocumentsDao.insert(iun,addressAlias,code,documentList);
            }
            CodeTimeToSend codeTimeToSend = new CodeTimeToSend(code, Duration.parse(time), additionalActions);
            notificationProgress.getCodeTimeToSendQueue().add(codeTimeToSend);
        }

        return notificationProgress;
    }

    // corregge i casi in cui il nome del documento arriva in maiuscolo
    private String formatDocumentCode(String code) {
        if (code == null)
            return code;

        if (code.equals("PLICO") )
            return "Plico";

        if (code.equals("INDAGINE") )
            return "Indagine";

        return code;
    }

    private List<AdditionalAction> getAdditionalActionsFromCode(String code) {
        List<AdditionalAction> res = new ArrayList<>();
        String additionalActionsRaw = code.substring(code.indexOf("[")+1,code.lastIndexOf("]"));
        for (String addActRaw :
                additionalActionsRaw.split(";")) {
            if (addActRaw.contains(":"))
                res.add(new AdditionalAction(AdditionalAction.ADDITIONAL_ACTIONS.valueOf(addActRaw.split(":")[0]),
                        addActRaw.split(":")[1]));
            else
                res.add(new AdditionalAction(AdditionalAction.ADDITIONAL_ACTIONS.valueOf(addActRaw), null));
        }
        return res;
    }

    private String getSequenceOfMicroAttempts(String receiverClean, String iun, String recipient) {
        Integer numberOfAttempts = notificationProgressDao.getNumberOfAttemptsByIun(iun, recipient);
        int index;
        if (numberOfAttempts == null) {
            index = 0;
        } else {
            index = numberOfAttempts;
        }
        notificationProgressDao.incrementNumberOfAttempt(iun, recipient);
        String[] messageProgress = receiverClean.split("_");
        if (notificationProgressDao.getNumberOfAttemptsByIun(iun, recipient) >= messageProgress.length) {
            //allora l'elemento processato è l'ultimo, quindi lo elimino dalla mappa
            notificationProgressDao.deleteNumberOfAttemptsByIun(iun, recipient);
        }
        return receiverClean.split("_")[index];

    }


    //example: MOCK-SEQU-WKHU-202209-P-1_send_digital_domicile0_source_PLATFORM_attempt_1
    //example: NRJT-MAWM-HJXN-202209-T-1_digital_delivering_progress_0_attempt_1_sourceSPECIAL_progidx_34
    // example: SEND_DIGITAL.IUN_TJKQ-NWHM-YQXW-202306-Z-1.RECINDEX_0.SOURCE_GENERAL.REPEAT_true.ATTEMPT_1
    private String getSequenceOfMacroAttempts(String receiverClean, String requestId) {
        int attemptIndex = requestId.indexOf("ATTEMPT_") + 8;
        int numberOfAttempts = Integer.parseInt(requestId.substring(attemptIndex, attemptIndex + 1));

        String[] attempts = receiverClean.split("attempt");
        
        if(numberOfAttempts == 1 && requestId.contains("REPEAT_false")){
            //Se si tratta del secondo tentativo effettivo (Non ripetizione del primo)
            try {
                //Ed è stata definita una sequenza apposita per la repeat, ritorno la sequenza apposita (la terza)
                return attempts[numberOfAttempts + 1];
            } catch (IndexOutOfBoundsException ex){
                //Se non è stata definita, andrà in IndexOutOfBoundsException e ritorno la seconda sequenza
                return attempts[numberOfAttempts];
            }
        }

        return attempts[numberOfAttempts];
    }

    //example: MOCK-LVRK-202302-G-1;RECINDEX_0;PCRETRY_0;ATTEMPT_1
    private String getSequenceOfPcRetry(String receiverClean, String requestId) {
        int pcRetryIndex = requestId.indexOf("PCRETRY_") + 8;
        int numberOfAttempts = Integer.parseInt(requestId.substring(pcRetryIndex, pcRetryIndex + 1));
        String[] attempts = receiverClean.split("pcretry");
        return attempts[numberOfAttempts];
    }

    private String getSequenceOfRetry(String receiverClean, String requestId) {
        int retryIndex = requestId.indexOf("PCRETRY_"); //ARRIVA PARTENDO DA 0 ? IL CODICE è AD UNA SOLA CIFRA?
        String numberOfRetryString = requestId.substring(retryIndex + 8, retryIndex + 9); //prendo il carattere successivo a PC_RETRY_
        int numberOfRetry = Integer.parseInt(numberOfRetryString);

        String[] split = receiverClean.split("@retry");
        String result = split[numberOfRetry >= split.length ? (split.length - 1) : numberOfRetry];
        return result.charAt(0) == '.' ? result.substring(1) : result;
    }

    private Optional<String> selectSequenceInParameter(String receiverAddress,String producType,List<String> parameterStoreName, NotificationProgress.PROGRESS_OUTPUT_CHANNEL source){
        List<Optional<EventCodeSequenceDTO[]>> sequenceEventCode = eventCodeSequenceParameterConsumer.getParameterValue(parameterStoreName, EventCodeSequenceDTO[].class);
        List<EventCodeSequenceDTO> eventCodeSequenceMergedList = new ArrayList<>();
        sequenceEventCode.stream()
                .filter(Optional::isPresent)
                .map(Optional::get)
                .forEach(eventCodeSequenceDTOS -> eventCodeSequenceMergedList.addAll(Arrays.asList(eventCodeSequenceDTOS)));

        if(eventCodeSequenceMergedList.isEmpty())  return Optional.empty();
        EventCodeSequenceDTO[] eventCodeSequenceList = eventCodeSequenceMergedList.toArray(new EventCodeSequenceDTO[]{});
        EventCodeSequenceDTO eventCodeSequenceDTO = null;
        log.info("Search for receiverAddress {}",receiverAddress);
        receiverAddress = receiverAddress.toLowerCase();
        if(receiverAddress.contains("@fail") && (source != NotificationProgress.PROGRESS_OUTPUT_CHANNEL.QUEUE_USER_ATTRIBUTES )){
            log.info("Enter in fail");
            String search = receiverAddress.substring(receiverAddress.lastIndexOf("fail")).trim();
            search = search.contains(" ")? search.substring(0,search.indexOf(" ")) : search;
            log.info("Search sequence {}",search);
            eventCodeSequenceDTO = searchInResult(eventCodeSequenceList, search.equals("fail")? search + "_" + producType : search);
            log.info("Find sequence {}",eventCodeSequenceDTO);

        }else if(receiverAddress.contains("@ok") || !receiverAddress.contains("@")){
            log.info("Enter in ok");
            String search = (receiverAddress.contains("@ok")? receiverAddress.substring(receiverAddress.lastIndexOf("ok")) : "ok").trim();
            search = search.contains(" ")? search.substring(0,search.indexOf(" ")) : search;
            log.info("Search sequence {}",search);
            eventCodeSequenceDTO = searchInResult(eventCodeSequenceList, search.equals("ok")? search + "_" + producType : search);
            log.info("Find sequence {}",eventCodeSequenceDTO);
        }

        return eventCodeSequenceDTO == null ? Optional.empty() : Optional.of(eventCodeSequenceDTO.sequence());
    }

    private EventCodeSequenceDTO searchInResult(EventCodeSequenceDTO[] eventCodeSequenceList, String searchName){
        return  Arrays.stream(eventCodeSequenceList).filter(e -> e.sequenceName().equalsIgnoreCase(searchName)).findAny().orElse(null);
    }

    private DiscoveredAddressEntity buildMockDiscoveredAddress(String sequence) {
        return new DiscoveredAddressEntity()
                .city("Milano")
                .address("via"+sequence)
                .name("Destinatario")
                .country("Italia")
                .cap("20121")
                .pr("MI");
    }

    private NotificationProgress.PROGRESS_OUTPUT_CHANNEL getOutputQueueFromSource(String source) {
        if (source == null)
            return NotificationProgress.PROGRESS_OUTPUT_CHANNEL.QUEUE_DELIVERY_PUSH;

        if (source.equals(pnExternalChannelsProperties.getCxIdDeliveryPush()))
            return NotificationProgress.PROGRESS_OUTPUT_CHANNEL.QUEUE_DELIVERY_PUSH;

        if (source.equals(pnExternalChannelsProperties.getCxIdUserAttributes()))
            return NotificationProgress.PROGRESS_OUTPUT_CHANNEL.QUEUE_USER_ATTRIBUTES;

        return NotificationProgress.PROGRESS_OUTPUT_CHANNEL.QUEUE_DELIVERY_PUSH;
    }


}
