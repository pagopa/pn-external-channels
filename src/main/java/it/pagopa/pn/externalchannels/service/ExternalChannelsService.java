package it.pagopa.pn.externalchannels.service;

import it.pagopa.pn.commons.abstractions.ParameterConsumer;
import it.pagopa.pn.externalchannels.config.aws.EventCodeSequenceDTO;
import it.pagopa.pn.externalchannels.dao.EventCodeDocumentsDao;
import it.pagopa.pn.externalchannels.dao.NotificationProgressDao;
import it.pagopa.pn.externalchannels.dto.AdditionalAction;
import it.pagopa.pn.externalchannels.dto.CodeTimeToSend;
import it.pagopa.pn.externalchannels.dto.NotificationProgress;
import it.pagopa.pn.externalchannels.model.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.Duration;
import java.time.Instant;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class ExternalChannelsService {

    private static final String IUN_ALREADY_EXISTS_MESSAGE = "[%s] Iun already inserted!";

    private static final List<String> OK_REQUEST_CODE_DIGITAL = List.of("C000", "C001", "C005", "C003");

    private static final List<String> FAIL_REQUEST_CODE_DIGITAL = List.of("C001", "C007", "C004");

    private static final List<String> OK_REQUEST_CODE_PAPER = List.of("CON080", "RECRN001A", "RECRN001B", "RECRN001C");

    private static final List<String> FAIL_REQUEST_CODE_PAPER = List.of("CON080", "RECRN002A", "RECRN002B", "RECRN002C");

    private static final String SEQUENCE_REGEXP = ".*@sequence\\.";

    private static final String DISCOVERED_MARKER = "@discovered";

    private final ParameterConsumer parameterConsumer;

    private static final String SEQUENCE_PARAMETER_NAME = "MapExternalChannelMockSequence";

    private final NotificationProgressDao notificationProgressDao;

    private final EventCodeDocumentsDao eventCodeDocumentsDao;


    public void sendDigitalLegalMessage(DigitalNotificationRequest digitalNotificationRequest, String appSourceName) {

        NotificationProgress notificationProgress = buildNotificationProgress(digitalNotificationRequest.getRequestId(),
                digitalNotificationRequest.getReceiverDigitalAddress(), NotificationProgress.PROGRESS_OUTPUT_CHANNEL.QUEUE_DELIVERY_PUSH, digitalNotificationRequest.getChannel().name(), FAIL_REQUEST_CODE_DIGITAL, OK_REQUEST_CODE_DIGITAL,
                selectSequenceInParameter(digitalNotificationRequest.getReceiverDigitalAddress(),digitalNotificationRequest.getChannel().getValue(),SEQUENCE_PARAMETER_NAME));

        boolean inserted = notificationProgressDao.insert(notificationProgress);

        if (! inserted) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, String.format(IUN_ALREADY_EXISTS_MESSAGE, digitalNotificationRequest.getRequestId()));
        }
    }

    public void sendDigitalCourtesyMessage(DigitalCourtesyMailRequest digitalCourtesyMailRequest, String appSourceName) {

        NotificationProgress notificationProgress = buildNotificationProgress(digitalCourtesyMailRequest.getRequestId(),
                digitalCourtesyMailRequest.getReceiverDigitalAddress(), NotificationProgress.PROGRESS_OUTPUT_CHANNEL.QUEUE_DELIVERY_PUSH, digitalCourtesyMailRequest.getChannel().name(), FAIL_REQUEST_CODE_DIGITAL, OK_REQUEST_CODE_DIGITAL, Optional.empty());

        boolean inserted = notificationProgressDao.insert(notificationProgress);

        if (! inserted) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, String.format(IUN_ALREADY_EXISTS_MESSAGE, digitalCourtesyMailRequest.getRequestId()));
        }
    }

    public void sendCourtesyShortMessage(DigitalCourtesySmsRequest digitalCourtesySmsRequest, String appSourceName) {

        NotificationProgress notificationProgress = buildNotificationProgress(digitalCourtesySmsRequest.getRequestId(),
                digitalCourtesySmsRequest.getReceiverDigitalAddress(), NotificationProgress.PROGRESS_OUTPUT_CHANNEL.QUEUE_DELIVERY_PUSH, digitalCourtesySmsRequest.getChannel().name(), FAIL_REQUEST_CODE_DIGITAL, OK_REQUEST_CODE_DIGITAL,Optional.empty());

        boolean inserted = notificationProgressDao.insert(notificationProgress);

        if (! inserted) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, String.format(IUN_ALREADY_EXISTS_MESSAGE, digitalCourtesySmsRequest.getRequestId()));
        }
    }

    public void sendPaperEngageRequest(PaperEngageRequest paperEngageRequest, NotificationProgress.PROGRESS_OUTPUT_CHANNEL output) {
        String address = paperEngageRequest.getReceiverAddress();

        NotificationProgress notificationProgress = buildNotificationProgress(paperEngageRequest.getRequestId(),
                address, output, paperEngageRequest.getProductType(), FAIL_REQUEST_CODE_PAPER, OK_REQUEST_CODE_PAPER,
                selectSequenceInParameter(address,paperEngageRequest.getProductType(),SEQUENCE_PARAMETER_NAME));

        boolean inserted = notificationProgressDao.insert(notificationProgress);

        if (! inserted) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, String.format(IUN_ALREADY_EXISTS_MESSAGE, paperEngageRequest.getRequestId()));
        }
    }



    private NotificationProgress buildNotificationProgress(String requestId, String receiverDigitalAddress,
                                                           NotificationProgress.PROGRESS_OUTPUT_CHANNEL output, String channel, List<String> failRequests, List<String> okRequests,Optional<String> requestSearched) {
        NotificationProgress notificationProgress;
        String iun = requestId;
        if (requestId.contains(".")) {
            iun = requestId.split("\\.")[1];
            iun = iun.contains("IUN_") ? iun.substring(iun.indexOf("IUN_") + 4) : iun;
        }

        if(requestSearched.isPresent()){
            notificationProgress = buildNotificationCustomized(requestSearched.get(), iun, requestId,receiverDigitalAddress);
        }else if (receiverDigitalAddress.contains("@fail") || receiverDigitalAddress.replaceFirst("\\+39", "").startsWith("001")) {
            notificationProgress = buildNotification(failRequests);
            if(receiverDigitalAddress.contains("discovered")) {
                notificationProgress.setDiscoveredAddress(buildMockDiscoveredAddress(""));
            }
        } else if (receiverDigitalAddress.contains("@sequence")) { //si presuppone che per gli sms non ci sia il caso sequence
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

        if (receiverClean.contains("attempt")) {
            receiverClean = getSequenceOfMacroAttempts(receiverClean, requestId);
        }
        if (receiverClean.contains("_")) {
            receiverClean = getSequenceOfMicroAttempts(receiverClean, iun, receiverDigitalAddress);
        }
        if(receiverClean.contains("retry")) {
            receiverClean = getSequenceOfRetry(receiverClean,requestId);
        }

        if(receiverClean.contains(DISCOVERED_MARKER)) {
            String discoveredSequence = receiverClean.substring(receiverClean.indexOf(DISCOVERED_MARKER));
            discoveredSequence = discoveredSequence.replace(DISCOVERED_MARKER, "@sequence");

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
                        .map(AdditionalAction::getInfo).toList();
                if (!documentList.isEmpty())
                    eventCodeDocumentsDao.insert(iun,addressAlias,code,documentList);
                code = code.substring(0,code.indexOf("["));
            }
            CodeTimeToSend codeTimeToSend = new CodeTimeToSend(code, Duration.parse(time), additionalActions);
            notificationProgress.getCodeTimeToSendQueue().add(codeTimeToSend);
        }

        return notificationProgress;
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
    private String getSequenceOfMacroAttempts(String receiverClean, String requestId) {
        int attemptIndex = requestId.indexOf("SENTATTEMPTMADE_");
        String numberOfAttemptsString = requestId.substring(attemptIndex + 16, attemptIndex + 17); //prendo il carattere successivo a attempt_
        int numberOfAttempts = Integer.parseInt(numberOfAttemptsString);

        String[] attempts = receiverClean.split("attempt");
        return attempts[numberOfAttempts >= attempts.length ? (attempts.length-1) : numberOfAttempts - 1];
    }

    private String getSequenceOfRetry(String receiverClean, String requestId) {
        int retryIndex = requestId.indexOf("PCRETRY_"); //ARRIVA PARTENDO DA 0 ? IL CODICE è AD UNA SOLA CIFRA?
        String numberOfRetryString = requestId.substring(retryIndex + 8, retryIndex + 9); //prendo il carattere successivo a PC_RETRY_
        int numberOfRetry = Integer.parseInt(numberOfRetryString);

        String[] split = receiverClean.split("@retry");
        String result = split[numberOfRetry >= split.length ? (split.length - 1) : numberOfRetry];
        return result.charAt(0) == '.' ? result.substring(1) : result;
    }

    private Optional<String> selectSequenceInParameter(String receiverAddress,String producType,String parameterStoreName){
        Optional<EventCodeSequenceDTO[]> sequenceEventCode = parameterConsumer.getParameterValue(parameterStoreName, EventCodeSequenceDTO[].class);
        if(sequenceEventCode.isEmpty())return Optional.empty();
        EventCodeSequenceDTO[] eventCodeSequenceList = sequenceEventCode.get();
        EventCodeSequenceDTO eventCodeSequenceDTO = null;
        log.info("Search for receiverAddress {}",receiverAddress);
        if(receiverAddress.contains("@fail")){
            log.info("Enter in fail");
            String search = receiverAddress.substring(receiverAddress.lastIndexOf("fail"));
            eventCodeSequenceDTO = searchInResult(eventCodeSequenceList, search.equals("fail")? search + "_" + producType : search);

        }else if(receiverAddress.contains("@ok") || !receiverAddress.contains("@")){
            log.info("Enter in ok");
            String search = receiverAddress.contains("@ok")? receiverAddress.substring(receiverAddress.lastIndexOf("ok")) : "ok";
            eventCodeSequenceDTO = searchInResult(eventCodeSequenceList, search.equals("ok")? search + "_" + producType : search);
        }
        log.info("Find sequence {}",eventCodeSequenceDTO);

        return eventCodeSequenceDTO == null ? Optional.empty() : Optional.of(eventCodeSequenceDTO.sequence());
    }

    private EventCodeSequenceDTO searchInResult(EventCodeSequenceDTO[] eventCodeSequenceList, String searchName){
        return  Arrays.stream(eventCodeSequenceList).filter(e -> e.sequenceName().equalsIgnoreCase(searchName)).findAny().orElse(null);
    }

    private DiscoveredAddress buildMockDiscoveredAddress(String sequence) {
        return new DiscoveredAddress()
                .city("Milan")
                .address("via"+sequence)
                .name("Milan")
                .country("Italy")
                .cap("20121")
                .pr("MI");
    }


}
