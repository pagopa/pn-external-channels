/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.pagopa.pn.externalchannels.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;

import it.pagopa.pn.externalchannels.binding.PnExtChnProcessor;
import it.pagopa.pn.externalchannels.event.eventinbound.InboundMessageType;
import it.pagopa.pn.externalchannels.util.TypeCanale;
import lombok.extern.slf4j.Slf4j;

import static it.pagopa.pn.api.dto.events.StandardEventHeader.PN_EVENT_HEADER_EVENT_TYPE;

/**
 *
 * @author GIANGR40
 */
@Slf4j
@Service
public class UnknownEventInboundService {

    @Autowired
    private InboundMessageType inboundMessageType;
    
    @Autowired
    private PnExtChnProcessor processor;

    @Autowired
    PnExtChnService pnExtChnService;

    
    @StreamListener(
            target = PnExtChnProcessor.INPUT,
            condition = "!T(it.pagopa.pn.externalchannels.event.eventinbound.InboundMessageType).PN_EXTCHN_PEC_MESSAGE_TYPE.equals(headers[T(it.pagopa.pn.api.dto.events.StandardEventHeader).PN_EVENT_HEADER_EVENT_TYPE])"
    )
    public void handleUnknownInboundEvent(@Payload String event,
            @Header(KafkaHeaders.RECEIVED_PARTITION_ID) int partition,
            @Header(name = PN_EVENT_HEADER_EVENT_TYPE, required = false) String tipoMessaggio) {

        if (tipoMessaggio == null) {
            tipoMessaggio = "unknown";
        }
        
        // TODO: Inserire chiamata al producer degli avanzamenti di stato
        
        pnExtChnService.scartaMessaggio(event, null);

        if (!inboundMessageType.checkIfKnown(tipoMessaggio)) {
            log.warn("Received unknown message type: " + tipoMessaggio + " from P-" + partition + ": message from kafka with id: " + event);
            throw new java.lang.IllegalStateException("Received unknown message type:" + tipoMessaggio + " from P" + partition + ": message from kafka: " + event);
        } // if

    }

}
