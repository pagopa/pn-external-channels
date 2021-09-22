package it.pagopa.pn.externalchannels.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import it.pagopa.pn.api.dto.events.*;

import java.time.Instant;

import static it.pagopa.pn.api.dto.events.MessageType.PN_EXT_CHN_PEC;

public class TestUtils {

    private static final ObjectMapper om = new ObjectMapper();

    public static String toJson(Object o){
        try {
            return om.writeValueAsString(o);
        } catch (Exception e) {
            return null;
        }
    }

    public static PnExtChnPecEvent mockPecMessage() {
        return PnExtChnPecEvent
                .builder()
                .header(StandardEventHeader
                        .builder()
                        .iun("123")
                        .eventType(PN_EXT_CHN_PEC)
                        .eventId("456")
                        .createdAt(Instant.now())
                        .publisher("pub")
                        .build()
                ).payload(PnExtChnPecEventPayload
                        .builder()
                        .iun("1")
                        .pecAddress("2")
                        .recipientDenomination("3")
                        .recipientTaxId("4")
                        .requestCorrelationId("5")
                        .senderDenomination("6")
                        .senderId("7")
                        .senderPecAddress("8")
                        .build()
                ).build();
    }

    public static PnExtChnPaperEvent mockPaperMessage() {
        return PnExtChnPaperEvent
                .builder()
                .header(StandardEventHeader
                        .builder()
                        .iun("123")
                        .eventType(PN_EXT_CHN_PEC)
                        .eventId("456")
                        .createdAt(Instant.now())
                        .publisher("pub")
                        .build()
                ).payload(PnExtChnPaperEventPayload
                        .builder()
                        .destinatario(PnExtChnPaperEventPayloadReceiver
                                .builder()
                                .cap("01234")
                                .provincia("PROVINCIA")
                                .comune("COMUNE")
                                .indirizzo("VIA PROVA 1/A")
                                .specificaIndirizzo("SPEC")
                                .presso("PRESSO")
                                .destinatario("dest")
                                .codiceFiscale("cf")
                                .build()
                        ).documento(PnExtChnPaperEventPayloadDocument
                                .builder()
                                .iun("123")
                                .codiceAtto("123")
                                .numeroCronologico(123)
                                .parteIstante("abc")
                                .procuratore("abc")
                                .ufficialeGiudiziario("abc")
                                .build()
                        ).mittente(PnExtChnPaperEventPayloadSender
                                .builder()
                                .paMittente("ABC")
                                .pecMittente("test@test.ts")
                                .build()
                        ).urlCallBack("url")
                        .build()
                ).build();
    }

}
