/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.pagopa.pn.externalchannels.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import it.pagopa.pn.api.dto.events.PnExtChnEmailEvent;
import it.pagopa.pn.api.dto.events.PnExtChnEmailEventPayload;
import it.pagopa.pn.api.dto.events.StandardEventHeader;
import it.pagopa.pn.externalchannels.binding.PnExtChnProcessor;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.validation.ConstraintViolation;
import javax.validation.Validator;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.Set;
import java.util.TimeZone;

import static it.pagopa.pn.api.dto.events.StandardEventHeader.*;
import static java.time.ZoneOffset.UTC;

/**
 *
 * @author GIANGR40
 */
@Service
@Slf4j
@NoArgsConstructor
@AllArgsConstructor
public class PnExtChnEmailEventInboundService {


    @Autowired
    private Jackson2ObjectMapperBuilder jackson2ObjectMapperBuilder;
    
    @Autowired
	private Validator validator;

    @Autowired
    PnExtChnService pnExtChnService;
    
    @Autowired
    PnExtChnProcessor processor;

    private ObjectMapper objectMapper;

    @PostConstruct()
    public void init() {
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
        objectMapper = jackson2ObjectMapperBuilder.createXmlMapper(false).build();
        objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        objectMapper.configure(SerializationFeature.WRITE_DATE_TIMESTAMPS_AS_NANOSECONDS, false);
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.setDateFormat(df);
        objectMapper.setTimeZone(TimeZone.getTimeZone(UTC));
    }

    @StreamListener(
            target = PnExtChnProcessor.NOTIF_PEC_INPUT,
            condition = "headers[T(it.pagopa.pn.api.dto.events.StandardEventHeader).PN_EVENT_HEADER_EVENT_TYPE]==T(it.pagopa.pn.api.dto.events.MessageType).PN_EXT_CHN_EMAIL"
    )
    public void handlePnExtChnEmailEvent(
            @Header(name = PN_EVENT_HEADER_PUBLISHER) String publisher,
            @Header(name = PN_EVENT_HEADER_EVENT_ID) String eventId,
            @Header(name = PN_EVENT_HEADER_EVENT_TYPE) String eventType,
            @Header(name = PN_EVENT_HEADER_IUN) String iun,
            @Header(name = PN_EVENT_HEADER_CREATED_AT) String createdAt,
            @Payload JsonNode event
    ) {

        log.info("PnExtChnEmailEventInboundService - handlePnExtChnEmailEvent - START");

        PnExtChnEmailEvent pnextchnpecevent = PnExtChnEmailEvent.builder()
            .header(StandardEventHeader.builder()
                    .publisher(publisher)
                    .eventId(eventId)
                    .eventType(eventType)
                    .iun(iun)
                    .createdAt(Instant.parse(createdAt))
                    .build()
            ).payload(objectMapper.convertValue(event, PnExtChnEmailEventPayload.class))
            .build();


        Set<ConstraintViolation<PnExtChnEmailEvent>> errors = null;
		errors = validator.validate(pnextchnpecevent);

        log.info("PnExtChnEmailEventInboundService - handlePnExtChnEmailEvent - END");
        // TODO: continue

    }
}
