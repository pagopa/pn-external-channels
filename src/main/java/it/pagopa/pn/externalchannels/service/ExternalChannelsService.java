package it.pagopa.pn.externalchannels.service;

import it.pagopa.pn.externalchannels.dao.NotificationProgressDao;
import it.pagopa.pn.externalchannels.dto.NotificationProgress;
import it.pagopa.pn.externalchannels.model.DigitalCourtesyMailRequest;
import it.pagopa.pn.externalchannels.model.DigitalCourtesySmsRequest;
import it.pagopa.pn.externalchannels.model.DigitalNotificationRequest;
import it.pagopa.pn.externalchannels.model.PaperEngageRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.Duration;
import java.time.Instant;
import java.util.LinkedList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ExternalChannelsService {

    private static final List<String> FAIL_REQUEST_CODE_DIGITAL = List.of("C001", "C007", "C004");

    private static final List<String> FAIL_REQUEST_CODE_PAPER = List.of("001", "002", "003", "005");

    private static final List<String> OK_REQUEST_CODE_DIGITAL = List.of("C000", "C001", "C005", "C003");

    private static final List<String> OK_REQUEST_CODE_PAPER = List.of("001", "002", "003", "004");

    private final NotificationProgressDao notificationProgressDao;


    public void sendDigitalLegalMessage(DigitalNotificationRequest digitalNotificationRequest, String appSourceName) {

        NotificationProgress notificationProgress = buildNotificationProgress(digitalNotificationRequest.getRequestId(),
                digitalNotificationRequest.getReceiverDigitalAddress(), appSourceName, digitalNotificationRequest.getChannel().name(), FAIL_REQUEST_CODE_DIGITAL, OK_REQUEST_CODE_DIGITAL);

        boolean inserted = notificationProgressDao.insert(notificationProgress);

        if (! inserted) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, String.format("[%s] Iun already inserted!", digitalNotificationRequest.getRequestId()));
        }
    }

    public void sendDigitalCourtesyMessage(DigitalCourtesyMailRequest digitalCourtesyMailRequest, String appSourceName) {

        NotificationProgress notificationProgress = buildNotificationProgress(digitalCourtesyMailRequest.getRequestId(),
                digitalCourtesyMailRequest.getReceiverDigitalAddress(), appSourceName, digitalCourtesyMailRequest.getChannel().name(), FAIL_REQUEST_CODE_DIGITAL, OK_REQUEST_CODE_DIGITAL);

        boolean inserted = notificationProgressDao.insert(notificationProgress);

        if (! inserted) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, String.format("[%s] Iun already inserted!", digitalCourtesyMailRequest.getRequestId()));
        }
    }

    public void sendCourtesyShortMessage(DigitalCourtesySmsRequest digitalCourtesySmsRequest, String appSourceName) {

        NotificationProgress notificationProgress = buildNotificationProgress(digitalCourtesySmsRequest.getRequestId(),
                digitalCourtesySmsRequest.getReceiverDigitalAddress(), appSourceName, digitalCourtesySmsRequest.getChannel().name(), FAIL_REQUEST_CODE_DIGITAL, OK_REQUEST_CODE_DIGITAL);

        boolean inserted = notificationProgressDao.insert(notificationProgress);

        if (! inserted) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, String.format("[%s] Iun already inserted!", digitalCourtesySmsRequest.getRequestId()));
        }
    }

    public void sendPaperEngageRequest(PaperEngageRequest paperEngageRequest, String appSourceName) {
        String address = paperEngageRequest.getReceiverAddressRow2() != null ? paperEngageRequest.getReceiverAddressRow2() : paperEngageRequest.getReceiverAddress();
        NotificationProgress notificationProgress = buildNotificationProgress(paperEngageRequest.getRequestId(),
                address, appSourceName, paperEngageRequest.getProductType(), FAIL_REQUEST_CODE_PAPER, OK_REQUEST_CODE_PAPER);

        boolean inserted = notificationProgressDao.insert(notificationProgress);

        if (! inserted) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, String.format("[%s] Iun already inserted!", paperEngageRequest.getRequestId()));
        }
    }

    private NotificationProgress buildNotification(List<String> codeToSend) {
        NotificationProgress notificationProgress = new NotificationProgress();
        notificationProgress.setCodeToSend(new LinkedList<>(codeToSend));
        notificationProgress.setTimeToSend(new LinkedList<>(List.of(Duration.ofSeconds(5), Duration.ofSeconds(5), Duration.ofSeconds(5), Duration.ofSeconds(5))));

        return notificationProgress;
    }

    private NotificationProgress buildNotificationCustomized(String receiverDigitalAddress, String iun, String requestId) {
        NotificationProgress notificationProgress = new NotificationProgress();
        notificationProgress.setCodeToSend(new LinkedList<>());
        notificationProgress.setTimeToSend(new LinkedList<>());

        String receiverClean = receiverDigitalAddress
                .replaceFirst(".*@sequence\\.", "");

        if(receiverClean.contains("attempt")) {
            receiverClean = getSequenceOfMacroAttempts(receiverClean, requestId);
        }
        if (receiverClean.contains("_")) {
            receiverClean = getSequenceOfMicroAttempts(receiverClean, iun);
        }

        String[] timeCodeCoupleArray = receiverClean.split("\\.");

        for (String timeCodeCouple : timeCodeCoupleArray) {
            String[] timeCodeCoupleSplit = timeCodeCouple.split("-");
            String time = "PT" + timeCodeCoupleSplit[0];
            String code = timeCodeCoupleSplit[1];
            notificationProgress.getTimeToSend().add(Duration.parse(time));
            notificationProgress.getCodeToSend().add(code);
        }

        return notificationProgress;
    }

    private String getSequenceOfMicroAttempts(String receiverClean, String iun) {
        Integer numberOfAttempts = notificationProgressDao.getNumberOfAttemptsByIun(iun);
        int index;
        if (numberOfAttempts == null) {
            index = 0;
        } else {
           index = numberOfAttempts;
        }
        notificationProgressDao.incrementNumberOfAttempt(iun);
        String[] messageProgress = receiverClean.split("_");
        if(notificationProgressDao.getNumberOfAttemptsByIun(iun) >= messageProgress.length ) {
            //allora l'elemento processato è l'ultimo, quindi lo elimino dalla mappa
            notificationProgressDao.deleteNumberOfAttemptsByIun(iun);
        }
        return receiverClean.split("_")[index];

    }


    private NotificationProgress buildNotificationProgress(String requestId, String receiverDigitalAddress, String appSourceName, String channel, List<String> failRequests, List<String> okRequests) {
        NotificationProgress notificationProgress;
        String iun = requestId.split("_")[0];

        if (receiverDigitalAddress.contains("@fail") || receiverDigitalAddress.replaceFirst("\\+39", "").startsWith("001")) {
            notificationProgress = buildNotification(failRequests);
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

        String finalString = receiverClean.split("attempt")[numberOfAttempts - 1];

        return finalString;
    }


}
