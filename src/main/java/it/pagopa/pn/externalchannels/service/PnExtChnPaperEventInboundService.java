/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.pagopa.pn.externalchannels.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import it.pagopa.pn.api.dto.events.PnExtChnPaperEvent;
import it.pagopa.pn.api.dto.events.PnExtChnPaperEventPayload;
import it.pagopa.pn.api.dto.events.StandardEventHeader;
import it.pagopa.pn.externalchannels.binding.PnExtChnProcessor;
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
public class PnExtChnPaperEventInboundService {

    @Autowired
    ObjectMapper objectMapper;
    
    @Autowired
	private Validator validator;

    @Autowired
    PnExtChnService pnExtChnService;
    
    @Autowired
    PnExtChnProcessor processor;

    @StreamListener(
            target = PnExtChnProcessor.NOTIF_PEC_INPUT,
            condition = "T(it.pagopa.pn.externalchannels.util.Util).eventTypeIs(headers, T(it.pagopa.pn.api.dto.events.EventType).SEND_PAPER_REQUEST)"
    )
    public void handlePnExtChnPaperEvent(
            @Header(name = PN_EVENT_HEADER_PUBLISHER) String publisher,
            @Header(name = PN_EVENT_HEADER_EVENT_ID) String eventId,
            @Header(name = PN_EVENT_HEADER_EVENT_TYPE) String eventType,
            @Header(name = PN_EVENT_HEADER_IUN) String iun,
            @Header(name = PN_EVENT_HEADER_CREATED_AT) String createdAt,
            @Payload JsonNode event
    ) {
        try {

            log.info("PnExtChnPaperEventInboundService - handlePnExtChnPaperEvent - START");

            PnExtChnPaperEvent pnextchnpecevent = PnExtChnPaperEvent.builder()
                    .header(StandardEventHeader.builder()
                            .publisher(publisher)
                            .eventId(eventId)
                            .eventType(eventType)
                            .iun(iun)
                            .createdAt(Instant.parse(createdAt))
                            .build()
                    ).payload(objectMapper.convertValue(event, PnExtChnPaperEventPayload.class))
                    .build();


            Set<ConstraintViolation<PnExtChnPaperEvent>> errors = null;
            errors = validator.validate(pnextchnpecevent);

            log.info("PnExtChnPaperEventInboundService - handlePnExtChnPaperEvent - END");
            // TODO: continue
        } catch(RuntimeException e) {
            log.error("PnExtChnPaperEventInboundService - handlePnExtChnPaperEvent", e);
            throw e;
        }
    }
}
