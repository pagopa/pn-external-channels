package it.pagopa.pn.externalchannels.service;


import it.pagopa.pn.api.dto.events.*;
import it.pagopa.pn.externalchannels.event.eventinbound.pnextchncartevent.PnExtChnCartEvent;
import it.pagopa.pn.externalchannels.util.Constants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;

import java.time.Instant;

import static it.pagopa.pn.api.dto.events.StandardEventHeader.*;

@Service
@Slf4j
@ConditionalOnProperty(name = "dev-options.fake-pn-ext-chn-service", havingValue = "true")
public class PnExtChnServiceFakeImpl extends PnExtChnServiceImpl {

	@Override
	public void salvaMessaggioCartaceo(PnExtChnCartEvent notificaCartacea) {
		super.salvaMessaggioCartaceo(notificaCartacea);
	}
	@Override
	public void salvaMessaggioDigitale(PnExtChnPecEvent notificaDigitale) {
		PnExtChnProgressStatusEvent out = computeResponse(notificaDigitale);
		if(out == null || out.getPayload().getStatusCode() == PnExtChnProgressStatus.OK)
			super.salvaMessaggioDigitale(notificaDigitale);
		else
			processor.statusMessage().send(
					MessageBuilder.withPayload(out.getPayload())
							.setHeader("partitionKey", Constants.ZERO_INT_VALUE)
							.setHeader(PN_EVENT_HEADER_IUN, out.getHeader().getIun())
							.setHeader(PN_EVENT_HEADER_EVENT_ID, out.getHeader().getEventId())
							.setHeader(PN_EVENT_HEADER_EVENT_TYPE, out.getHeader().getEventType())
							.setHeader(PN_EVENT_HEADER_CREATED_AT, out.getHeader().getCreatedAt())
							.setHeader(PN_EVENT_HEADER_PUBLISHER, out.getHeader().getPublisher())
							.build()
			);
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
