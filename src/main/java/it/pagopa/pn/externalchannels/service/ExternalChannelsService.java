package it.pagopa.pn.externalchannels.service;

import it.pagopa.pn.externalchannels.dao.NotificationProgressDao;
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
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ExternalChannelsService {

    private static final String IUN_ALREADY_EXISTS_MESSAGE = "[%s] Iun already inserted!";

    private static final List<String> FAIL_REQUEST_CODE_DIGITAL = List.of("C001", "C007", "C004");

    private static final List<String> FAIL_REQUEST_CODE_PAPER = List.of("001", "002", "003", "005");

    private static final List<String> OK_REQUEST_CODE_DIGITAL = List.of("C000", "C001", "C005", "C003");

    private static final List<String> OK_REQUEST_CODE_PAPER = List.of("001", "002", "003", "004");

    private static final String SEQUENCE_REGEXP = ".*@sequence\\.";

    private final NotificationProgressDao notificationProgressDao;


    public void sendDigitalLegalMessage(DigitalNotificationRequest digitalNotificationRequest, String appSourceName) {

        NotificationProgress notificationProgress = buildNotificationProgress(digitalNotificationRequest.getRequestId(),
                digitalNotificationRequest.getReceiverDigitalAddress(), appSourceName, digitalNotificationRequest.getChannel().name(), FAIL_REQUEST_CODE_DIGITAL, OK_REQUEST_CODE_DIGITAL);

        boolean inserted = notificationProgressDao.insert(notificationProgress);

        if (! inserted) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, String.format(IUN_ALREADY_EXISTS_MESSAGE, digitalNotificationRequest.getRequestId()));
        }
    }

    public void sendDigitalCourtesyMessage(DigitalCourtesyMailRequest digitalCourtesyMailRequest, String appSourceName) {

        NotificationProgress notificationProgress = buildNotificationProgress(digitalCourtesyMailRequest.getRequestId(),
                digitalCourtesyMailRequest.getReceiverDigitalAddress(), appSourceName, digitalCourtesyMailRequest.getChannel().name(), FAIL_REQUEST_CODE_DIGITAL, OK_REQUEST_CODE_DIGITAL);

        boolean inserted = notificationProgressDao.insert(notificationProgress);

        if (! inserted) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, String.format(IUN_ALREADY_EXISTS_MESSAGE, digitalCourtesyMailRequest.getRequestId()));
        }
    }

    public void sendCourtesyShortMessage(DigitalCourtesySmsRequest digitalCourtesySmsRequest, String appSourceName) {

        NotificationProgress notificationProgress = buildNotificationProgress(digitalCourtesySmsRequest.getRequestId(),
                digitalCourtesySmsRequest.getReceiverDigitalAddress(), appSourceName, digitalCourtesySmsRequest.getChannel().name(), FAIL_REQUEST_CODE_DIGITAL, OK_REQUEST_CODE_DIGITAL);

        boolean inserted = notificationProgressDao.insert(notificationProgress);

        if (! inserted) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, String.format(IUN_ALREADY_EXISTS_MESSAGE, digitalCourtesySmsRequest.getRequestId()));
        }
    }

    public void sendPaperEngageRequest(PaperEngageRequest paperEngageRequest, String appSourceName) {
        String address = paperEngageRequest.getReceiverAddress();
        NotificationProgress notificationProgress = buildNotificationProgress(paperEngageRequest.getRequestId(),
                address, appSourceName, paperEngageRequest.getProductType(), FAIL_REQUEST_CODE_PAPER, OK_REQUEST_CODE_PAPER);

        boolean inserted = notificationProgressDao.insert(notificationProgress);

        if (! inserted) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, String.format(IUN_ALREADY_EXISTS_MESSAGE, paperEngageRequest.getRequestId()));
        }
    }

    private NotificationProgress buildNotification(List<String> codeToSendList) {
        NotificationProgress notificationProgress = new NotificationProgress();
        List<CodeTimeToSend> codeTimeToSends = codeToSendList.stream().map(codeToSend -> new CodeTimeToSend(codeToSend,
                Duration.ofSeconds(5))).collect(Collectors.toList());
        notificationProgress.setCodeTimeToSendQueue(new LinkedList<>(codeTimeToSends));


        return notificationProgress;
    }

    private NotificationProgress buildNotificationCustomized(String receiverDigitalAddress, String iun, String requestId) {
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

        String[] timeCodeCoupleArray = receiverClean.split("\\.");

        for (String timeCodeCouple : timeCodeCoupleArray) {
            String[] timeCodeCoupleSplit = timeCodeCouple.split("-");
            String time = "PT" + timeCodeCoupleSplit[0];
            String code = timeCodeCoupleSplit[1];
            CodeTimeToSend codeTimeToSend = new CodeTimeToSend(code, Duration.parse(time));
            notificationProgress.getCodeTimeToSendQueue().add(codeTimeToSend);
        }

        return notificationProgress;
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


    private NotificationProgress buildNotificationProgress(String requestId, String receiverDigitalAddress, String appSourceName, String channel, List<String> failRequests, List<String> okRequests) {
        NotificationProgress notificationProgress;
        String iun = requestId.split("_")[0];

        if (receiverDigitalAddress.contains("@fail") || receiverDigitalAddress.replaceFirst("\\+39", "").startsWith("001")) {
            notificationProgress = buildNotification(failRequests);
            if(receiverDigitalAddress.contains("discovered")) {
                notificationProgress.setDiscoveredAddress(buildMockDiscoveredAddress());
            }
        } else if (receiverDigitalAddress.contains("@sequence")) { //si presuppone che per gli sms non ci sia il caso sequence
            notificationProgress = buildNotificationCustomized(receiverDigitalAddress, iun, requestId);
        } else {
            notificationProgress = buildNotification(okRequests);
        }

        notificationProgress.setRequestId(requestId);
        notificationProgress.setDestinationAddress(receiverDigitalAddress);
        notificationProgress.setCreateMessageTimestamp(Instant.now());
        notificationProgress.setAppSourceName(appSourceName);
        notificationProgress.setIun(iun);
        notificationProgress.setChannel(channel);

        return notificationProgress;

    }

    //example: MOCK-SEQU-WKHU-202209-P-1_send_digital_domicile0_source_PLATFORM_attempt_1
    //example: NRJT-MAWM-HJXN-202209-T-1_digital_delivering_progress_0_attempt_1_sourceSPECIAL_progidx_34
    private String getSequenceOfMacroAttempts(String receiverClean, String requestId) {
        int attemptIndex = requestId.indexOf("attempt_");
        String numberOfAttemptsString = requestId.substring(attemptIndex + 8, attemptIndex + 9); //prendo il carattere successivo a attempt_
        int numberOfAttempts = Integer.parseInt(numberOfAttemptsString);

        return receiverClean.split("attempt")[numberOfAttempts - 1];
    }

    private DiscoveredAddress buildMockDiscoveredAddress() {
        return new DiscoveredAddress()
                .city("Milan")
                .address("via Roma")
                .name("Milan")
                .country("Italy")
                .cap("20121")
                .pr("MI");
    }


}
