package it.pagopa.pn.externalchannels.service;

import it.pagopa.pn.externalchannels.dao.NotificationProgressDao;
import it.pagopa.pn.externalchannels.dto.NotificationProgress;
import it.pagopa.pn.externalchannels.model.DigitalCourtesyMailRequest;
import it.pagopa.pn.externalchannels.model.DigitalNotificationRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.LinkedList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ExternalChannelsService {

    private static final List<String> FAIL_REQUEST_CODE = List.of("C001", "C007", "C004");

    private static final List<String> OK_REQUEST_CODE = List.of("C000", "C001", "C005", "C003");

    private final NotificationProgressDao notificationProgressDao;


    public void sendDigitalLegalMessage(DigitalNotificationRequest digitalNotificationRequest, String appSourceName) {

        NotificationProgress notificationProgress = buildNotificationProgress(digitalNotificationRequest.getRequestId(),
                digitalNotificationRequest.getReceiverDigitalAddress(), appSourceName);

        notificationProgressDao.insert(notificationProgress);
        log.info("NotificationProgress saved: {}", notificationProgress);
    }

    public void sendDigitalCourtesyMessage(DigitalCourtesyMailRequest digitalCourtesyMailRequest, String appSourceName) {

        NotificationProgress notificationProgress = buildNotificationProgress(digitalCourtesyMailRequest.getRequestId(), digitalCourtesyMailRequest.getReceiverDigitalAddress(), appSourceName);
        notificationProgressDao.insert(notificationProgress);
        log.info("NotificationProgress saved: {}", notificationProgress);
    }


    private NotificationProgress buildNotificationFail() {
        NotificationProgress notificationProgress = new NotificationProgress();
        notificationProgress.setCodeToSend(new LinkedList<>(FAIL_REQUEST_CODE));
        notificationProgress.setTimeToSend(new LinkedList<>(List.of(Duration.ofSeconds(5), Duration.ofSeconds(5), Duration.ofSeconds(5))));
        return notificationProgress;
    }

    private NotificationProgress buildNotificationSuccess() {
        NotificationProgress notificationProgress = new NotificationProgress();
        notificationProgress.setCodeToSend(new LinkedList<>(OK_REQUEST_CODE));
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

    private NotificationProgress buildNotificationProgress(String requestId, String receiverDigitalAddress, String appSourceName) {
        NotificationProgress notificationProgress;
        String iun = requestId.split("_")[0];

        if (receiverDigitalAddress.contains("@fail")) {
            notificationProgress = buildNotificationFail();
        } else if (receiverDigitalAddress.contains("@sequence")) {
            notificationProgress = buildNotificationCustomized(receiverDigitalAddress, iun, requestId);
        } else {
            notificationProgress = buildNotificationSuccess();
        }

        notificationProgress.setRequestId(requestId);
        notificationProgress.setDestinationAddress(receiverDigitalAddress);
        notificationProgress.setCreateMessageTimestamp(Instant.now());
        notificationProgress.setAppSourceName(appSourceName);
        notificationProgress.setIun(iun);

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
