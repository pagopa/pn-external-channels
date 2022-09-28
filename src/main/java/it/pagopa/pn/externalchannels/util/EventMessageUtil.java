package it.pagopa.pn.externalchannels.util;

import it.pagopa.pn.api.dto.events.EventPublisher;
import it.pagopa.pn.api.dto.events.StandardEventHeader;
import it.pagopa.pn.externalchannels.event.PnDeliveryPushCourtesyEvent;
import it.pagopa.pn.externalchannels.event.PnDeliveryPushPaperEvent;
import it.pagopa.pn.externalchannels.event.PnDeliveryPushPecEvent;
import it.pagopa.pn.externalchannels.model.*;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public class EventMessageUtil {

    private static final List<String> ERROR_CODES = List.of("C004", "C008", "C009", "C010");
    public static final List<String> LEGAL_CHANNELS = List.of("PEC", "REM");

    public static final List<String> PAPER_CHANNELS = List.of("__AR__", "__890__", "__RI__", "__RS__");

    private static final String OK_CODE = "C003";

    public static SingleStatusUpdate buildMessageEvent(String code, String requestId, String channel, String destinationAddress) {
        if (LEGAL_CHANNELS.contains(channel)) {
            return buildLegalMessage(code, requestId);
        } else if (PAPER_CHANNELS.contains(channel)) {
            return buildPaperMessage(code, requestId.split( "_")[0], requestId, channel, destinationAddress);
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
                                .generatedMessage(new DigitalMessageReference().system("mock-system").id("mock-" + UUID.randomUUID()))
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
                                .generatedMessage(new DigitalMessageReference().system("mock-system").id("mock-" + UUID.randomUUID()))
                );
    }

    private static SingleStatusUpdate buildPaperMessage(String code, String iun, String requestId, String productType, String destinationAddress) {

        return new SingleStatusUpdate()
                .analogMail(
                        new PaperProgressStatusEvent()
                                .iun(iun)
                                .discoveredAddress(new DiscoveredAddress().name("MockName").city("MockCity").address(destinationAddress))
                                .requestId(requestId)
                                .productType(productType)
                                .clientRequestTimeStamp(OffsetDateTime.now())
                                .attachments(null)
                                .statusCode("__" + code + "__")
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

    public static PnDeliveryPushPecEvent buildPecEvent(SingleStatusUpdate event, String iun) {
        return PnDeliveryPushPecEvent.builder()
                .header(StandardEventHeader.builder()
                        .iun(iun)
                        .eventId(event.getDigitalLegal().getRequestId())
                        .eventType("EXTERNAL_CHANNELS_EVENT")
                        .publisher(EventPublisher.EXTERNAL_CHANNELS.name())
                        .createdAt(Instant.now())
                        .build())
                .payload(event)
                .build();
    }

    public static PnDeliveryPushCourtesyEvent buildCourtesyEvent(SingleStatusUpdate event, String iun) {
        return PnDeliveryPushCourtesyEvent.builder()
                .header(StandardEventHeader.builder()
                        .iun(iun)
                        .eventId(event.getDigitalCourtesy().getRequestId())
                        .eventType("EXTERNAL_CHANNELS_EVENT")
                        .publisher(EventPublisher.EXTERNAL_CHANNELS.name())
                        .createdAt(Instant.now())
                        .build())
                .payload(event)
                .build();
    }

    public static PnDeliveryPushPaperEvent buildPaperEvent(SingleStatusUpdate event, String iun) {
        return PnDeliveryPushPaperEvent.builder()
                .header(StandardEventHeader.builder()
                        .iun(iun)
                        .eventId(event.getAnalogMail().getRequestId())
                        .eventType("EXTERNAL_CHANNELS_EVENT")
                        .publisher(EventPublisher.EXTERNAL_CHANNELS.name())
                        .createdAt(Instant.now())
                        .build())
                .payload(event)
                .build();
    }


}
