/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.pagopa.pn.externalchannels.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import it.pagopa.pn.externalchannels.event.eventinbound.InboundMessageType;
import it.pagopa.pn.externalchannels.event.eventinbound.pnextchnpecevent.PnExtChnPecEvent;
import it.pagopa.pn.externalchannels.event.eventinbound.pnextchnpecevent.PnExtChnPecEventHeader;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;
import it.pagopa.pn.externalchannels.event.eventinbound.pnextchnpecevent.PnExtChnPecEventPayload;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import static java.time.ZoneOffset.UTC;
import java.util.TimeZone;
import javax.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import it.pagopa.pn.extchannels.binding.PNExtChnInboundSink;

/**
 *
 * @author GIANGR40
 */
@Service
@Slf4j
public class PnExtChnPecEventInboundService {


    @Autowired
    private Jackson2ObjectMapperBuilder jackson2ObjectMapperBuilder;

    private ObjectMapper objectMapper;

    @PostConstruct
    public void init() {
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
        objectMapper = jackson2ObjectMapperBuilder.createXmlMapper(false).build();
        objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        objectMapper.configure(SerializationFeature.WRITE_DATE_TIMESTAMPS_AS_NANOSECONDS, false);
        objectMapper.setDateFormat(df);
        objectMapper.setTimeZone(TimeZone.getTimeZone(UTC));
    }

    @StreamListener(
            target = PNExtChnInboundSink.INPUT,
            condition = "headers['"
            + InboundMessageType.KAFKA_HEADER_MESSAGETYPE
            + "']=='"
            + InboundMessageType.PN_EXTCHN_PEC_MESSAGE_TYPE
            + "'"
    )
    public void handlePnExtChnPecEvent(
            @Header(name = KafkaHeaders.RECEIVED_PARTITION_ID) int partition,
            /*@Header(name=KafkaHeaders.ACKNOWLEDGMENT) Acknowledgment acknowledgement,*/
            @Header(name = InboundMessageType.KAFKA_HEADER_PUBLISHER) String publisher,
            @Header(name = InboundMessageType.KAFKA_HEADER_MESSAGEID) String messageId,
            @Header(name = InboundMessageType.KAFKA_HEADER_MESSAGETYPE) String messageType,
            @Header(name = InboundMessageType.KAFKA_HEADER_PARTITIONKEY) String partitionKey,
            @Payload JsonNode event
    ) {
        
        //log.debug("Received a new message from P-{} : message :\"{}\"", partition,event.toString());         

        PnExtChnPecEvent pnextchnpecevent
                = PnExtChnPecEvent.builder()
                        .pnExtChnPecEventHeader(PnExtChnPecEventHeader.builder()
                                        .publisher(publisher)
                                        .messageId(messageId)
                                        .messageType(messageType)
                                        .partitionKey(partitionKey)
                                        .build()
                        )
                        .pnExtChnPecEventPayload(objectMapper.convertValue(event, PnExtChnPecEventPayload.class)
                        ).build();
        log.debug("Received message from P-" + partition + ": message from kafka: " + pnextchnpecevent.toString());        
        log.debug("object = {}", objectMapper.valueToTree(pnextchnpecevent));

    }

}
