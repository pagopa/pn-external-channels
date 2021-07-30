/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.pagopa.pn.externalchannels.service;

import it.pagopa.pn.externalchannels.event.eventinbound.InboundMessageType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;
import it.pagopa.pn.extchannels.binding.PNExtChnInboundSink;

/**
 *
 * @author GIANGR40
 */
@Slf4j
@Service
public class UnknownEventInboundService {

    @Autowired
    private InboundMessageType inboundMessageType;

    @StreamListener(
            target = PNExtChnInboundSink.INPUT
    )
    public void handleUnknownInboundEvent(@Payload String event,
            @Header(KafkaHeaders.RECEIVED_PARTITION_ID) int partition,
            @Header(name = InboundMessageType.KAFKA_HEADER_MESSAGETYPE, required = false) String tipoMessaggio) {

        if (tipoMessaggio == null) {
            tipoMessaggio = "unknown";
        }

        if (!inboundMessageType.checkIfKnown(tipoMessaggio)) {
            log.warn("Received unknown message type: " + tipoMessaggio + " from P-" + partition + ": message from kafka with id: " + event);
            throw new java.lang.IllegalStateException("Received unknown message type:" + tipoMessaggio + " from P" + partition + ": message from kafka: " + event);
        } // if

    }

}
