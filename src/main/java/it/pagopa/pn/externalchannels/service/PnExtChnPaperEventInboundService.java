/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.pagopa.pn.externalchannels.service;

import com.amazonaws.services.sqs.AmazonSQSAsync;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.awspring.cloud.core.env.ResourceIdResolver;
import io.awspring.cloud.messaging.core.QueueMessagingTemplate;
import it.pagopa.pn.api.dto.events.*;
import it.pagopa.pn.externalchannels.binding.PnExtChnProcessor;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
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
    private ObjectMapper objectMapper;
    
    @Autowired
	private Validator validator;

    @Autowired
    private PnExtChnService pnExtChnService;
    
    @Autowired
    private PnExtChnProcessor processor;

    @Autowired
    private EventSenderService evtSender;

    @Value("${spring.cloud.stream.bindings." + PnExtChnProcessor.STATUS_OUTPUT + ".destination}")
    private String statusMessageQueue;




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
            evtSender.sendTo( statusMessageQueue, PnExtChnProgressStatusEvent.builder()
                    .header( builder()
                            .publisher(EventPublisher.EXTERNAL_CHANNELS.name())
                            .eventId(eventId + "_resp")
                            .eventType(EventType.SEND_PEC_RESPONSE.name())
                            .iun(iun)
                            .createdAt(Instant.now())
                            .build()
                    )
                    .payload( PnExtChnProgressStatusEventPayload.builder()
                            .iun( iun )
                            .statusDate( Instant.now() )
                            .statusCode( PnExtChnProgressStatus.OK )
                            .requestCorrelationId( eventId )
                            .build()
                    )
                    .build()
            );

        } catch(RuntimeException e) {
            log.error("PnExtChnPaperEventInboundService - handlePnExtChnPaperEvent", e);
            throw e;
        }
    }
}
