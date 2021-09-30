/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.pagopa.pn.externalchannels.service;

import it.pagopa.pn.externalchannels.binding.PnExtChnProcessor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

import static it.pagopa.pn.api.dto.events.StandardEventHeader.PN_EVENT_HEADER_EVENT_TYPE;

/**
 *
 * @author GIANGR40
 */
@Slf4j
@Service
public class UnknownEventInboundService {

    @Autowired
    PnExtChnService pnExtChnService;
    
    @StreamListener(
            target = PnExtChnProcessor.NOTIF_PEC_INPUT,
            condition = "!T(it.pagopa.pn.externalchannels.util.Util).eventTypeIsKnown(headers)"
    )
    public void handleUnknownInboundEvent(
            @Payload String event,
            @Header(name = PN_EVENT_HEADER_EVENT_TYPE, required = false) String tipoMessaggio
    ) {
        log.info("UnknownEventInboundService - handleUnknownInboundEvent - START");

        if (tipoMessaggio == null) {
            tipoMessaggio = "unknown";
        }
        
        // TODO: Inserire chiamata al producer degli avanzamenti di stato
        
        pnExtChnService.discardMessage(event, null);

        log.warn("Received unknown message type: " + tipoMessaggio + " from sqs: " + event);

        log.info("UnknownEventInboundService - handleUnknownInboundEvent - END");
    }

}
