package it.pagopa.pn.externalchannels.controller;

import it.pagopa.pn.api.dto.events.PnExtChnPecEvent;
import it.pagopa.pn.api.dto.events.PnExtChnPecEventPayload;
import it.pagopa.pn.api.dto.events.StandardEventHeader;
import it.pagopa.pn.externalchannels.event.eventinbound.InboundMessageType;
import it.pagopa.pn.externalchannels.event.eventinbound.pnextchncartevent.PnExtChnCartEvent;
import it.pagopa.pn.externalchannels.event.eventinbound.pnextchncartevent.PnExtChnCartEventHeader;
import it.pagopa.pn.externalchannels.event.eventinbound.pnextchncartevent.PnExtChnCartEventPayload;
import it.pagopa.pn.externalchannels.service.PnExtChnService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.time.Instant;

import static it.pagopa.pn.api.dto.events.StandardEventHeader.*;

@Slf4j
@Valid
@RestController
@RequestMapping("/external-channel")
public class PnExtChnController {
	
	@Autowired
	private PnExtChnService pnExtChnService;
	
	
	// TODO: In caso di errore di validazione, bisogna inviare il messaggio sempre sul topic di errore ?
	
	@PostMapping(path = "/cartacea/saveNotifica")
	public Object saveNotificaCartacea(
			@RequestHeader(name = InboundMessageType.KAFKA_HEADER_PUBLISHER) String publisher,
            @RequestHeader(name = InboundMessageType.KAFKA_HEADER_MESSAGEID) String messageId,
            @RequestHeader(name = InboundMessageType.KAFKA_HEADER_MESSAGETYPE) String messageType,
            @RequestHeader(name = InboundMessageType.KAFKA_HEADER_PARTITIONKEY) String partitionKey,
			@Valid @RequestBody PnExtChnCartEventPayload notificaCartacea) {
		log.info("PnExtChnController - saveNotificaCartacea - START");
		PnExtChnCartEvent pnextchncartevent =
				PnExtChnCartEvent.builder()
				.pnExtChnCartEventHeader(PnExtChnCartEventHeader.builder()
						.publisher(publisher)
						.messageId(messageId)
						.messageType(messageType)
						.partitionKey(partitionKey)
						.build()
						).pnExtChnCartEventPayload(notificaCartacea).build();

		pnExtChnService.salvaMessaggioCartaceo(pnextchncartevent);
		log.info("PnExtChnController - saveNotificaCartacea - END");
		return notificaCartacea;
	}
	
	@PostMapping(path = "/digitale/saveNotifica")
	public Object saveNotificaDigitale(
			@RequestHeader(name = PN_EVENT_HEADER_PUBLISHER) String publisher,
            @RequestHeader(name = PN_EVENT_HEADER_EVENT_ID) String eventId,
            @RequestHeader(name = PN_EVENT_HEADER_EVENT_TYPE) String eventType,
            @RequestHeader(name = PN_EVENT_HEADER_IUN) String iun,
			@RequestHeader(name = PN_EVENT_HEADER_CREATED_AT) String createdAt,
			@Valid @RequestBody PnExtChnPecEventPayload notificaDigitale) {
		log.info("PnExtChnController - saveNotificaDigitale - START");
		
		PnExtChnPecEvent pnextchnpecevent =
				PnExtChnPecEvent.builder()
		        	.header(StandardEventHeader.builder()
						.publisher(publisher)
						.eventId(eventId)
						.eventType(eventType)
						.iun(iun)
						.createdAt(Instant.now())
						.build()
					).payload(notificaDigitale).build();
		
		pnExtChnService.salvaMessaggioDigitale(pnextchnpecevent);
		log.info("PnExtChnController - saveNotificaDigitale - END");
		return notificaDigitale;
	}

}
