package it.pagopa.pn.externalchannels.schedule;

import it.pagopa.pn.externalchannels.dao.NotificationProgressDao;
import it.pagopa.pn.externalchannels.dto.NotificationProgress;
import it.pagopa.pn.externalchannels.model.BaseMessageProgressEvent;
import it.pagopa.pn.externalchannels.model.DigitalMessageReference;
import it.pagopa.pn.externalchannels.model.ProgressEventCategory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class MessageScheduler {

    private static final List<String> ERROR_CODES = List.of("C004", "C008", "C009", "C010");

    private static final String OK_CODE = "C003";

    private final NotificationProgressDao dao;


    @Scheduled(cron = "${job.cron-expression}")
    public void run() {
        log.debug("[{}] CLOCK!", Instant.now());
        Collection<NotificationProgress> all = dao.findAll();
        Instant now = Instant.now();
        for (NotificationProgress notificationProgress : all) {
            Instant messageTimestamp = notificationProgress.getLastMessageSentTimestamp() == null ? notificationProgress.getCreateMessageTimestamp()
                    : notificationProgress.getLastMessageSentTimestamp();

            long seconds = notificationProgress.getTimeToSend().peek().getSeconds();
            Instant messageTimestampPlusSeconds = messageTimestamp.plusSeconds(seconds);
            if (now.getEpochSecond() >= messageTimestampPlusSeconds.getEpochSecond()) {
                String code = notificationProgress.getCodeToSend().poll();
                notificationProgress.getTimeToSend().poll();
                String requestId = notificationProgress.getRequestId();
                String iun = notificationProgress.getIun();

                BaseMessageProgressEvent baseMessageProgressEvent = buildBaseMessageProgressEvent(code, requestId);

                log.info("Message to send: {}", baseMessageProgressEvent);
                notificationProgress.setLastMessageSentTimestamp(Instant.now());

                if (notificationProgress.getCodeToSend().isEmpty()) {
                    dao.delete(iun);
                    log.info("Deleted message with requestId: {}", iun);
                }
            }
        }
    }

    private BaseMessageProgressEvent buildBaseMessageProgressEvent(String code, String requestId) {

        return new BaseMessageProgressEvent()
                .eventCode(BaseMessageProgressEvent.EventCodeEnum.fromValue(code))
                .requestId(requestId)
                .status(buildStatus(code))
                .eventTimestamp(OffsetDateTime.now())
                .generatedMessage(new DigitalMessageReference().system("mock-system").id("mock-" + UUID.randomUUID()));
    }

    private ProgressEventCategory buildStatus(String code) {
        if (code.equals(OK_CODE)) {
            return ProgressEventCategory.OK;
        }
        if (ERROR_CODES.contains(code)) {
            return ProgressEventCategory.ERROR;
        }
        return ProgressEventCategory.PROGRESS;
    }
}
