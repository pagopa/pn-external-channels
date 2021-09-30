/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.pagopa.pn.externalchannels.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import it.pagopa.pn.api.dto.events.*;
import it.pagopa.pn.externalchannels.binding.PnExtChnProcessor;
import it.pagopa.pn.externalchannels.util.Constants;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

import javax.validation.ConstraintViolation;
import javax.validation.Validator;
import java.time.Instant;
import java.util.Set;

import static it.pagopa.pn.api.dto.events.StandardEventHeader.*;

/**
 *
 * @author GIANGR40
 */
@Service
@Slf4j
@NoArgsConstructor
@AllArgsConstructor
public class PnExtChnPecEventInboundService {

    @Autowired
	private Validator validator;

    @Autowired
    PnExtChnService pnExtChnService;
    
    @Autowired
    PnExtChnProcessor processor;

    @Autowired
    ObjectMapper objectMapper;

    @StreamListener(
            target = PnExtChnProcessor.NOTIF_PEC_INPUT,
            condition = "T(it.pagopa.pn.externalchannels.util.Util).eventTypeIs(headers, T(it.pagopa.pn.api.dto.events.EventType).SEND_PEC_REQUEST)"
    )
    public void handlePnExtChnPecEvent(
            @Header(name = PN_EVENT_HEADER_PUBLISHER) String publisher,
            @Header(name = PN_EVENT_HEADER_EVENT_ID) String eventId,
            @Header(name = PN_EVENT_HEADER_EVENT_TYPE) String eventType,
            @Header(name = PN_EVENT_HEADER_IUN) String iun,
            @Header(name = PN_EVENT_HEADER_CREATED_AT) String createdAt,
            @Payload JsonNode event
    ) {
        try {

            log.info("PnExtChnPecEventInboundService - handlePnExtChnPecEvent - START");

            PnExtChnPecEvent pnextchnpecevent = PnExtChnPecEvent.builder()
                    .header(StandardEventHeader.builder()
                            .publisher(publisher)
                            .eventId(eventId)
                            .eventType(eventType)
                            .iun(iun)
                            .createdAt(Instant.parse(createdAt))
                            .build()
                    ).payload(objectMapper.convertValue(event, PnExtChnPecEventPayload.class))
                    .build();

            Set<ConstraintViolation<PnExtChnPecEvent>> errors = validator.validate(pnextchnpecevent);

            if (!errors.isEmpty()) {
                log.error(Constants.MSG_ERRORI_DI_VALIDAZIONE);
                // Invio il messaggio di errore su topic dedicato
                pnExtChnService.produceStatusMessage("",
                        pnextchnpecevent
                                .getPayload().getIun(),
                        EventType.SEND_PEC_RESPONSE, PnExtChnProgressStatus.PERMANENT_FAIL, null, 1, null, null);
                // Salvo il messaggio di scartato su una struttura DB dedicata
                pnExtChnService.discardMessage(event.asText(), errors);
            } else {

                log.debug("Received message from sqs: " + pnextchnpecevent.toString());
                log.debug("object = {}", objectMapper.valueToTree(pnextchnpecevent));

                pnExtChnService.saveDigitalMessage(pnextchnpecevent);
            }

            log.info("PnExtChnPecEventInboundService - handlePnExtChnPecEvent - END");

        } catch (RuntimeException e) {
            log.error("PnExtChnPecEventInboundService - handlePnExtChnPecEvent", e);
            throw e;
        }
    }

}
