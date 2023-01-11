package it.pagopa.pn.externalchannels.util;

import it.pagopa.pn.api.dto.events.EventPublisher;
import it.pagopa.pn.api.dto.events.StandardEventHeader;
import it.pagopa.pn.externalchannels.dto.CodeTimeToSend;
import it.pagopa.pn.externalchannels.dto.NotificationProgress;
import it.pagopa.pn.externalchannels.event.PaperChannelEvent;
import it.pagopa.pn.externalchannels.event.PnDeliveryPushEvent;
import it.pagopa.pn.externalchannels.model.*;
import lombok.extern.slf4j.Slf4j;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Slf4j
public class EventMessageUtil {

    private EventMessageUtil() {}

    private static final String MOCK_PREFIX = "mock-";

    private static final List<String> ERROR_CODES = List.of("C004", "C008", "C009", "C010");
    public static final List<String> LEGAL_CHANNELS = List.of("PEC", "REM");
    public static final String AR = "AR";

    public static final List<String> PAPER_CHANNELS = List.of(AR, "890", "RI", "RS", "RN_AR");

    private static final String OK_CODE = "C003";

    public static SingleStatusUpdate buildMessageEvent(NotificationProgress notificationProgress) {
        boolean isLastMessage = notificationProgress.getCodeTimeToSendQueue().size() == 1;
        CodeTimeToSend codeTimeToSend = notificationProgress.getCodeTimeToSendQueue().poll();
        log.debug("[{}] Processing codeTimeToSend: {}", notificationProgress.getIun(), codeTimeToSend);
        assert codeTimeToSend != null;

        String code = codeTimeToSend.getCode();
        String requestId = notificationProgress.getRequestId();
        String channel = notificationProgress.getChannel();
        DiscoveredAddress discoveredAddress = null;

        if (LEGAL_CHANNELS.contains(channel)) {
            return buildLegalMessage(code, requestId);
        } else if (PAPER_CHANNELS.contains(channel)) {

            if(isLastMessage && notificationProgress.getDiscoveredAddress() != null) {
                discoveredAddress = notificationProgress.getDiscoveredAddress();
            }

            return buildPaperMessage(code, requestId.split( "_")[0], requestId, channel, discoveredAddress);
        }

        return buildDigitalCourtesyMessage(code, requestId);
    }

    private static SingleStatusUpdate buildLegalMessage(String code, String requestId) {

        return new SingleStatusUpdate()
                .digitalLegal(
                        new LegalMessageSentDetails()
                                .eventCode(LegalMessageSentDetails.EventCodeEnum.fromValue(code))
                                .requestId(requestId)
                                .status(buildStatus(code))
                                .eventTimestamp(OffsetDateTime.now())
                                .generatedMessage(new DigitalMessageReference().system("mock-system").id(MOCK_PREFIX + UUID.randomUUID()))
                );
    }

    private static SingleStatusUpdate buildDigitalCourtesyMessage(String code, String requestId) {

        return new SingleStatusUpdate()
                .digitalCourtesy(
                        new CourtesyMessageProgressEvent()
                                .eventCode(CourtesyMessageProgressEvent.EventCodeEnum.fromValue(code))
                                .requestId(requestId)
                                .status(buildStatus(code))
                                .eventTimestamp(OffsetDateTime.now())
                                .generatedMessage(new DigitalMessageReference().system("mock-system").id(MOCK_PREFIX + UUID.randomUUID()))
                );
    }

    private static SingleStatusUpdate buildPaperMessage(String code, String iun, String requestId, String productType, DiscoveredAddress discoveredAddress) {
        return new SingleStatusUpdate()
                .analogMail(
                        new PaperProgressStatusEvent()
                                .iun(iun)
                                .discoveredAddress(discoveredAddress)
                                .requestId(requestId)
                                .productType(productType)
                                .clientRequestTimeStamp(OffsetDateTime.now())
                                .attachments(null)
                                .statusCode(code)
                                .statusDateTime(OffsetDateTime.now())
                                .statusDescription("Mock status"));
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

}
