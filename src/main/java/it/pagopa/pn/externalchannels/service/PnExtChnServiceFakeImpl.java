package it.pagopa.pn.externalchannels.service;


import it.pagopa.pn.api.dto.events.*;
import it.pagopa.pn.externalchannels.arubapec.ArubaSenderService;
import it.pagopa.pn.externalchannels.arubapec.SimpleMessage;
import it.pagopa.pn.externalchannels.util.MessageUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import it.pagopa.pn.api.dto.events.EventPublisher;
import it.pagopa.pn.api.dto.events.EventType;
import it.pagopa.pn.api.dto.events.PnExtChnPaperEvent;
import it.pagopa.pn.api.dto.events.PnExtChnPecEvent;
import it.pagopa.pn.api.dto.events.PnExtChnProgressStatus;
import it.pagopa.pn.api.dto.events.PnExtChnProgressStatusEvent;
import it.pagopa.pn.api.dto.events.PnExtChnProgressStatusEventPayload;
import it.pagopa.pn.api.dto.events.StandardEventHeader;

import java.time.Instant;
import java.util.Map;

@Service
@Slf4j
@ConditionalOnProperty(name = "dev-options.fake-pn-ext-chn-service", havingValue = "true")
public class PnExtChnServiceFakeImpl extends PnExtChnServiceImpl {

	@Autowired
	private ArubaSenderService pecSvc;

	@Autowired
	private MessageUtil msgUtils;

	@Override
	public void savePaperMessage(PnExtChnPaperEvent notificaCartacea) {
		log.info("PnExtChnServiceFakeImpl - savePaperMessage - START");
		super.savePaperMessage(notificaCartacea);
		log.info("PnExtChnServiceFakeImpl - savePaperMessage - END");
	}
	@Override
	public void saveDigitalMessage(PnExtChnPecEvent notificaDigitale) {
		log.info("PnExtChnServiceFakeImpl - saveDigitalMessage - START OLD");

		if( notificaDigitale.getPayload().getPecAddress().endsWith(".real") ) {
			realSend(notificaDigitale);
		}
		else {
			fakeSend(notificaDigitale);
		}
	}

	private void realSend(PnExtChnPecEvent notificaDigitale) {
		String iun = notificaDigitale.getPayload().getIun();

		String pecAddress = notificaDigitale.getPayload().getPecAddress().replaceFirst("\\.real$", "");

		String content = msgUtils.pecPayloadToMessage( notificaDigitale.getPayload(), MessageBodyType.PLAIN_TEXT );
		pecSvc.sendMessage( SimpleMessage.builder()
				.iun(iun)
				.eventId( notificaDigitale.getHeader().getEventId() )
				.senderAddress("no-replay@pn.it")
				.recipientAddress(pecAddress)
				.subject("Notifica Digitale da PN " + iun)
				.contentType("text/plain; charset=UTF-8")
				.content( content )
				.build()
		);
	}


	private void fakeSend(PnExtChnPecEvent notificaDigitale) {
		PnExtChnProgressStatusEvent out = computeResponse(notificaDigitale);
		if(out == null || out.getPayload().getStatusCode() == PnExtChnProgressStatus.OK) {
			try {
				// super.saveDigitalMessage(notificaDigitale);
				out = buildResponse(notificaDigitale, PnExtChnProgressStatus.OK);
				Map<String, Object> headers = headersToMap(out.getHeader());
				log.info("PnExtChnServiceFakeImpl - saveDigitalMessage - before push ok");
				statusQueueMessagingTemplate.convertAndSend(statusMessageQueue, out.getPayload(), headers);
				log.info("PnExtChnServiceFakeImpl - saveDigitalMessage - ok");
			}
			catch ( RuntimeException exc ) {
				log.error("PnExtChnServiceFakeImpl - saveDigitalMessage - pushError", exc);
			}
		}
		else {
			Map<String, Object> headers = headersToMap(out.getHeader());
			log.info("PnExtChnServiceFakeImpl - saveDigitalMessage - before push fail old");
			try {
				statusQueueMessagingTemplate.convertAndSend(statusMessageQueue, out.getPayload(), headers);
			}
			catch ( RuntimeException exc ) {
				log.error("PnExtChnServiceFakeImpl - saveDigitalMessage - pushError", exc);
			}
			log.info("PnExtChnServiceFakeImpl - saveDigitalMessage - failed");
		}
		log.info("PnExtChnServiceFakeImpl - saveDigitalMessage - END");
	}


	private PnExtChnProgressStatusEvent computeResponse(PnExtChnPecEvent evt) {
		PnExtChnProgressStatus outcome = decideOutcome( evt );
		return outcome != null ? buildResponse( evt, outcome ) : null;
	}

	private PnExtChnProgressStatusEvent buildResponse(PnExtChnPecEvent evt, PnExtChnProgressStatus status) {
		return PnExtChnProgressStatusEvent.builder()
				.header( StandardEventHeader.builder()
						.eventId( evt.getHeader().getEventId() + "_response" )
						.iun( evt.getHeader().getIun() )
						.eventType( EventType.SEND_PEC_RESPONSE.name() )
						.createdAt( Instant.now() )
						.publisher( EventPublisher.EXTERNAL_CHANNELS.name() )
						.build()
				)
				.payload(PnExtChnProgressStatusEventPayload.builder()
						.canale("PEC")
						.requestCorrelationId( evt.getPayload().getRequestCorrelationId() )
						.iun( evt.getHeader().getIun() )
						.statusCode( status )
						.statusDate( Instant.now() )
						.build()
				)
				.build();
	}

	private PnExtChnProgressStatus decideOutcome( PnExtChnPecEvent evt ) {
		PnExtChnProgressStatus status;

		String eventId = evt.getHeader().getEventId();
		String retryNumberPart = eventId.replaceFirst(".*([0-9]+)$", "$1");

		String pecAddress = evt.getPayload().getPecAddress();
		if( pecAddress != null ) {
			String domainPart = pecAddress.replaceFirst(".*@", "");

			if( domainPart.startsWith("fail-both")
					|| (domainPart.startsWith("fail-first") && "1".equals( retryNumberPart )) ) {
				status = PnExtChnProgressStatus.RETRYABLE_FAIL;
			}
			else if( domainPart.startsWith("do-not-exists") ) {
				status = PnExtChnProgressStatus.PERMANENT_FAIL;
			}
			else {
				status = PnExtChnProgressStatus.OK;
			}
		}
		else {
			status = null;
		}

		return status;
	}

}
