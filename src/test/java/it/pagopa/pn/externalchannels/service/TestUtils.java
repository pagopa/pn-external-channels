package it.pagopa.pn.externalchannels.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JSR310Module;
import it.pagopa.pn.api.dto.events.*;
import it.pagopa.pn.api.dto.notification.address.PhysicalAddress;
import it.pagopa.pn.externalchannels.entities.resultdescriptor.ResultDescriptor;
import it.pagopa.pn.externalchannels.pojos.ElaborationResult;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;

import static it.pagopa.pn.api.dto.events.EventType.*;

public class TestUtils {

    private static final ObjectMapper om;

    static {
        om = new ObjectMapper();
        JSR310Module javaTimeModule = new JSR310Module();
        om.registerModule(javaTimeModule);
    }

    public static String toJson(Object o) {
        try {
            return om.writeValueAsString(o);
        } catch (Exception e) {
            return null;
        }
    }

    public static PnExtChnEmailEvent mockEmailMessage() {
        return PnExtChnEmailEvent
                .builder()
                .header(StandardEventHeader
                        .builder()
                        .iun("123")
                        .eventType(SEND_PEC_REQUEST.name())
                        .eventId("456")
                        .createdAt(Instant.now())
                        .publisher("pub")
                        .build()
                ).payload(PnExtChnEmailEventPayload
                        .builder()
                        .iun("1")
                        .emailAddress("2")
                        .recipientDenomination("3")
                        .recipientTaxId("4")
                        .senderDenomination("6")
                        .senderId("7")
                        .senderEmailAddress("8")
                        .shipmentDate(Instant.now())
                        .accessUrl("9")
                        .build()
                ).build();
    }

    public static PnExtChnPecEvent mockPecMessage() {
        return mockPecMessage("1", "2");
    }

    public static PnExtChnPecEvent mockPecMessage(String iun, String address) {
        return PnExtChnPecEvent
                .builder()
                .header(StandardEventHeader
                        .builder()
                        .iun("123")
                        .eventType(SEND_PEC_REQUEST.name())
                        .eventId("456")
                        .createdAt(Instant.now())
                        .publisher("pub")
                        .build()
                ).payload(PnExtChnPecEventPayload
                        .builder()
                        .iun(iun)
                        .pecAddress(address)
                        .recipientDenomination("3")
                        .recipientTaxId("4")
                        .requestCorrelationId("5")
                        .senderDenomination("6")
                        .senderId("7")
                        .senderPecAddress("8")
                        .shipmentDate(Instant.now())
                        .accessUrl("9")
                        .build()
                ).build();
    }

    public static PnExtChnPaperEvent mockPaperMessage() {
        return mockPaperMessage("123", "via abc 1");
    }

    public static PnExtChnPaperEvent mockPaperMessage(String iun, String address) {
        return PnExtChnPaperEvent
                .builder()
                .header(StandardEventHeader
                        .builder()
                        .iun("123")
                        .eventType(SEND_PAPER_REQUEST.name())
                        .eventId("456")
                        .createdAt(Instant.now())
                        .publisher("pub")
                        .build()
                ).payload(PnExtChnPaperEventPayload
                        .builder()
                        .requestCorrelationId("123")
                        .iun(iun)
                        .destinationAddress(PhysicalAddress.builder()
                                .address(address)
                                .addressDetails("")
                                .at("")
                                .zip("80100")
                                .municipality("ROMA")
                                .province("RM")
                                .foreignState("")
                        .build())
                        .recipientDenomination("3")
                        .senderDenomination("4")
                        .serviceLevel(ServiceLevelType.REGISTERED_LETTER_890)
                        .communicationType(CommunicationType.FAILED_DELIVERY_NOTICE)
                        .accessUrl("5")
                        .build()
                ).build();
    }

    public static List<ElaborationResult> mockElaborationResults() {
        return Arrays.asList(
                ElaborationResult.builder().iun("123").result("1").eventId("123").build(),
                ElaborationResult.builder().iun("456").result("2").eventId("456").build()
        );
    }

    public static List<ResultDescriptor> mockResultDescriptors(){
        return Arrays.asList(
                new ResultDescriptor("1", "1", true, false),
                new ResultDescriptor("2", "2", false, false),
                new ResultDescriptor("3", "3", false, true)
        );
    }

}
