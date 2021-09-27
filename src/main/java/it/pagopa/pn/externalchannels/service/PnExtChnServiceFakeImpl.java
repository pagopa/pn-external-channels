package it.pagopa.pn.externalchannels.service;


import com.amazonaws.services.sqs.AmazonSQSAsync;
import it.pagopa.pn.api.dto.events.*;
import it.pagopa.pn.externalchannels.service.fake.PnExtChnProgressStatusEventProducer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

import static it.pagopa.pn.api.dto.events.StandardEventHeader.*;

@Service
@Slf4j
@ConditionalOnProperty(name = "dev-options.fake-pn-ext-chn-service", havingValue = "true")
public class PnExtChnServiceFakeImpl extends PnExtChnServiceImpl {

	public PnExtChnServiceFakeImpl(AmazonSQSAsync sqsClient) {
		super(sqsClient);
	}

	PnExtChnProgressStatusEventProducer outQueue;

	@Override
	public void savePaperMessage(PnExtChnPaperEvent notificaCartacea) {
		log.info("PnExtChnServiceFakeImpl - savePaperMessage - START");
		super.savePaperMessage(notificaCartacea);
		log.info("PnExtChnServiceFakeImpl - savePaperMessage - END");
	}
	@Override
	public void saveDigitalMessage(PnExtChnPecEvent notificaDigitale) {
		log.info("PnExtChnServiceFakeImpl - saveDigitalMessage - START");

		PnExtChnProgressStatusEvent out = computeResponse(notificaDigitale);
		if(out == null || out.getPayload().getStatusCode() == PnExtChnProgressStatus.OK) {
			// super.saveDigitalMessage(notificaDigitale);
			out = buildResponse(notificaDigitale, PnExtChnProgressStatus.OK);
			Map<String, Object> headers = headersToMap(out.getHeader());
			log.info("PnExtChnServiceFakeImpl - saveDigitalMessage - before push ok");
			outQueue.push( out );
			//queueMessagingTemplate.convertAndSend(statusMessageQueue, out, headers);
			log.info("PnExtChnServiceFakeImpl - saveDigitalMessage - ok");
		}
		else {
			Map<String, Object> headers = headersToMap(out.getHeader());
			//queueMessagingTemplate.convertAndSend(statusMessageQueue, out, headers);
			log.info("PnExtChnServiceFakeImpl - saveDigitalMessage - before push fail");
			try {
				outQueue.push( out );
			}
			catch ( RuntimeException exc ) {
				log.error("PnExtChnServiceFakeImpl - saveDigitalMessage - pushError", exc);
			}
			log.info("PnExtChnServiceFakeImpl - saveDigitalMessage - failed");
		}
		log.info("PnExtChnServiceFakeImpl - saveDigitalMessage - END");
	}

	private Map<String, Object> headersToMap(StandardEventHeader header) {
		HashMap<String, Object> map = new HashMap<>();
		map.put(PN_EVENT_HEADER_IUN, header.getIun());
		map.put(PN_EVENT_HEADER_EVENT_ID, header.getEventId());
		map.put(PN_EVENT_HEADER_EVENT_TYPE, header.getEventType());
		map.put(PN_EVENT_HEADER_CREATED_AT, header.getCreatedAt().toString());
		map.put(PN_EVENT_HEADER_PUBLISHER, header.getPublisher());
		return map;
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
