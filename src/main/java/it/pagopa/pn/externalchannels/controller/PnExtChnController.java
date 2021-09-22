package it.pagopa.pn.externalchannels.controller;

import it.pagopa.pn.api.dto.events.*;
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
	
	@PostMapping(path = "/paper/saveNotification")
	public Object savePaperNotification(
			@RequestHeader(name = PN_EVENT_HEADER_PUBLISHER) String publisher,
			@RequestHeader(name = PN_EVENT_HEADER_EVENT_ID) String eventId,
			@RequestHeader(name = PN_EVENT_HEADER_EVENT_TYPE) String eventType,
			@RequestHeader(name = PN_EVENT_HEADER_IUN) String iun,
			@RequestHeader(name = PN_EVENT_HEADER_CREATED_AT) String createdAt,
			@Valid @RequestBody PnExtChnPaperEventPayload paperNotification) {
		log.info("PnExtChnController - savePaperNotification - START");
		PnExtChnPaperEvent pnextchncartevent =
				PnExtChnPaperEvent.builder()
						.header(StandardEventHeader.builder()
								.publisher(publisher)
								.eventId(eventId)
								.eventType(eventType)
								.iun(iun)
								.createdAt(Instant.parse(createdAt))
								.build()
						).payload(paperNotification).build();

		pnExtChnService.savePaperMessage(pnextchncartevent);
		log.info("PnExtChnController - savePaperNotification - END");
		return paperNotification;
	}
	
	@PostMapping(path = "/digital/saveNotification")
	public Object saveDigitalNotification(
			@RequestHeader(name = PN_EVENT_HEADER_PUBLISHER) String publisher,
            @RequestHeader(name = PN_EVENT_HEADER_EVENT_ID) String eventId,
            @RequestHeader(name = PN_EVENT_HEADER_EVENT_TYPE) String eventType,
            @RequestHeader(name = PN_EVENT_HEADER_IUN) String iun,
			@RequestHeader(name = PN_EVENT_HEADER_CREATED_AT) String createdAt,
			@Valid @RequestBody PnExtChnPecEventPayload digitalNotification) {
		log.info("PnExtChnController - saveDigitalNotification - START");
		
		PnExtChnPecEvent pnextchnpecevent =
				PnExtChnPecEvent.builder()
		        	.header(StandardEventHeader.builder()
						.publisher(publisher)
						.eventId(eventId)
						.eventType(eventType)
						.iun(iun)
						.createdAt(Instant.parse(createdAt))
						.build()
					).payload(digitalNotification).build();
		
		pnExtChnService.saveDigitalMessage(pnextchnpecevent);
		log.info("PnExtChnController - saveDigitalNotification - END");
		return digitalNotification;
	}

}
