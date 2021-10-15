/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.pagopa.pn.externalchannels.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import it.pagopa.pn.api.dto.events.*;
import it.pagopa.pn.api.dto.notification.address.PhysicalAddress;
import it.pagopa.pn.externalchannels.binding.PnExtChnProcessor;
import it.pagopa.pn.externalchannels.util.Constants;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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
    EventSenderService evtSenderSvc;

    @Autowired
    PnExtChnProcessor processor;

    @Value("${spring.cloud.stream.bindings." + PnExtChnProcessor.STATUS_OUTPUT + ".destination}")
    String statusMessageQueue;


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

            PnExtChnPaperEvent pnextchnpaperevent = PnExtChnPaperEvent.builder()
                    .header(StandardEventHeader.builder()
                            .publisher(publisher)
                            .eventId(eventId)
                            .eventType(eventType)
                            .iun(iun)
                            .createdAt(Instant.parse(createdAt))
                            .build()
                    ).payload(objectMapper.convertValue(event, PnExtChnPaperEventPayload.class))
                    .build();


            Set<ConstraintViolation<PnExtChnPaperEvent>> errors = validator.validate(pnextchnpaperevent);

            if (!errors.isEmpty()) {
                log.error(Constants.MSG_ERRORI_DI_VALIDAZIONE);
                // Invio il messaggio di errore su topic dedicato
                pnExtChnService.produceStatusMessage("",
                        pnextchnpaperevent
                                .getPayload().getIun(),
                        EventType.SEND_PAPER_RESPONSE, PnExtChnProgressStatus.PERMANENT_FAIL, null, 1, null, null);
                // Salvo il messaggio di scartato su una struttura DB dedicata
                pnExtChnService.discardMessage(event.asText(), errors);
            } else {

                log.debug("Received message from sqs: " + pnextchnpaperevent.toString());
                log.debug("object = {}", objectMapper.valueToTree(pnextchnpaperevent));

                final PhysicalAddress destinationAddress = pnextchnpaperevent.getPayload().getDestinationAddress();
                final String address = destinationAddress.getAddress();
                if( address.contains("ImmediateResponse(NEW_ADDR:")) {
                    String newAdrress = address.replaceFirst(".*ImmediateResponse\\(NEW_ADDR:([^)]*).*", "$1");
                    log.info("PnExtChnPaperEventInboundService - handlePnExtChnPaperEvent - END");
                    evtSenderSvc.sendTo( statusMessageQueue, PnExtChnProgressStatusEvent.builder()
                            .header( builder()
                                    .publisher(EventPublisher.EXTERNAL_CHANNELS.name())
                                    .eventId(eventId + "_resp")
                                    .eventType(EventType.SEND_PAPER_RESPONSE.name())
                                    .iun(iun)
                                    .createdAt(Instant.now())
                                    .build()
                            )
                            .payload( PnExtChnProgressStatusEventPayload.builder()
                                    .iun( iun )
                                    .statusDate( Instant.now() )
                                    .statusCode( PnExtChnProgressStatus.RETRYABLE_FAIL )
                                    .newPhysicalAddress(destinationAddress.toBuilder().address(newAdrress).build())
                                    .requestCorrelationId( eventId )
                                    .build()
                            )
                            .build()
                    );

                }
                else if( address.contains("ImmediateResponse_FAIL")) {
                    log.info("PnExtChnPaperEventInboundService - handlePnExtChnPaperEvent - END");
                    evtSenderSvc.sendTo( statusMessageQueue, PnExtChnProgressStatusEvent.builder()
                            .header( builder()
                                    .publisher(EventPublisher.EXTERNAL_CHANNELS.name())
                                    .eventId(eventId + "_resp")
                                    .eventType(EventType.SEND_PAPER_RESPONSE.name())
                                    .iun(iun)
                                    .createdAt(Instant.now())
                                    .build()
                            )
                            .payload( PnExtChnProgressStatusEventPayload.builder()
                                    .iun( iun )
                                    .statusDate( Instant.now() )
                                    .statusCode( PnExtChnProgressStatus.PERMANENT_FAIL )
                                    .requestCorrelationId( eventId )
                                    .build()
                            )
                            .build()
                    );

                }
                else if( address.contains("ImmediateResponse_OK")) {
                    log.info("PnExtChnPaperEventInboundService - handlePnExtChnPaperEvent - END");
                    evtSenderSvc.sendTo( statusMessageQueue, PnExtChnProgressStatusEvent.builder()
                            .header( builder()
                                    .publisher(EventPublisher.EXTERNAL_CHANNELS.name())
                                    .eventId(eventId + "_resp")
                                    .eventType(EventType.SEND_PAPER_RESPONSE.name())
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

                }
                else {
                    pnExtChnService.savePaperMessage(pnextchnpaperevent);
                }

            }

            log.info("PnExtChnPaperEventInboundService - handlePnExtChnPaperEvent - END");
            
        } catch(RuntimeException e) {
            log.error("PnExtChnPaperEventInboundService - handlePnExtChnPaperEvent", e);
            throw e;
        }
    }

}
