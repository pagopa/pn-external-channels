/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.pagopa.pn.externalchannels.service;

import static it.pagopa.pn.api.dto.events.StandardEventHeader.*;
import static java.time.ZoneOffset.UTC;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.Set;
import java.util.TimeZone;

import javax.annotation.PostConstruct;
import javax.validation.ConstraintViolation;
import javax.validation.Validator;

import it.pagopa.pn.api.dto.events.PnExtChnPecEvent;
import it.pagopa.pn.api.dto.events.PnExtChnPecEventPayload;
import it.pagopa.pn.api.dto.events.PnExtChnProgressStatus;
import it.pagopa.pn.api.dto.events.StandardEventHeader;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import it.pagopa.pn.externalchannels.binding.PnExtChnProcessor;
import it.pagopa.pn.externalchannels.event.eventinbound.InboundMessageType;
import it.pagopa.pn.externalchannels.util.Constants;
import it.pagopa.pn.externalchannels.util.TypeCanale;
import lombok.extern.slf4j.Slf4j;

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
    private Jackson2ObjectMapperBuilder jackson2ObjectMapperBuilder;
    
    @Autowired
	private Validator validator;

    @Autowired
    PnExtChnService pnExtChnService;
    
    @Autowired
    PnExtChnProcessor processor;

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
            target = PnExtChnProcessor.INPUT,
            condition = "headers[T(it.pagopa.pn.api.dto.events.StandardEventHeader).PN_EVENT_HEADER_EVENT_TYPE]==T(it.pagopa.pn.externalchannels.event.eventinbound.InboundMessageType).PN_EXTCHN_PEC_MESSAGE_TYPE"
    )
    public void handlePnExtChnPecEvent(
            @Header(name = KafkaHeaders.RECEIVED_PARTITION_ID) int partition,
            /*@Header(name=KafkaHeaders.ACKNOWLEDGMENT) Acknowledgment acknowledgement,*/
            @Header(name = PN_EVENT_HEADER_PUBLISHER) String publisher,
            @Header(name = PN_EVENT_HEADER_EVENT_ID) String eventId,
            @Header(name = PN_EVENT_HEADER_EVENT_TYPE) String eventType,
            @Header(name = PN_EVENT_HEADER_IUN) String iun,
            @Header(name = PN_EVENT_HEADER_CREATED_AT) String createdAt,
            @Payload JsonNode event
    ) {
        
        //log.debug("Received a new message from P-{} : message :\"{}\"", partition,event.toString());         

        PnExtChnPecEvent pnextchnpecevent = PnExtChnPecEvent.builder()
            .header(StandardEventHeader.builder()
                    .publisher(publisher)
                    .eventId(eventId)
                    .eventType(eventType)
                    .iun(iun)
                    .createdAt(Instant.now())
                    .build()
            ).payload(objectMapper.convertValue(event, PnExtChnPecEventPayload.class))
            .build();
        
        
        Set<ConstraintViolation<PnExtChnPecEvent>> errors = null;
		errors = validator.validate(pnextchnpecevent);
		
		if(!errors.isEmpty()) {
			log.error(Constants.MSG_ERRORI_DI_VALIDAZIONE);
			// Invio il messaggio di errore su topic dedicato
			pnExtChnService.produceStatusMessage(pnextchnpecevent
	        		.getPayload()
	        		.getCodiceAtto(),
	        		pnextchnpecevent
	        		.getPayload().getIun(),
					null, PnExtChnProgressStatus.PERMANENT_FAIL, TypeCanale.PEC, 1, null, null, null, null);
			// Salvo il messaggio di scartato su una struttura DB dedicata
			pnExtChnService.scartaMessaggio(event.asText(), errors);
		} else {

	        log.debug("Received message from P-" + partition + ": message from kafka: " + pnextchnpecevent.toString());        
	        log.debug("object = {}", objectMapper.valueToTree(pnextchnpecevent));
	        
	        pnExtChnService.salvaMessaggioDigitale(pnextchnpecevent);
		}
        

    }
}
