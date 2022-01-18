package it.pagopa.pn.externalchannels.service.pnextchnservice;


import it.pagopa.pn.api.dto.events.*;
import it.pagopa.pn.api.dto.notification.address.PhysicalAddress;
import it.pagopa.pn.externalchannels.pecbysmtp.SenderService;
import it.pagopa.pn.externalchannels.pecbysmtp.SimpleMessage;
import it.pagopa.pn.externalchannels.config.properties.S3Properties;
import it.pagopa.pn.externalchannels.service.MessageBodyType;
import it.pagopa.pn.externalchannels.service.PnExtChnFileTransferService;
import it.pagopa.pn.externalchannels.util.MessageUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Arrays;
import java.util.Date;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@Slf4j
public class PnExtChnServiceFakeImpl extends PnExtChnServiceImpl {

	private static final String IMMEDIATE_RESPONSE_NEW_ADDR_REGEX = "^ImmediateResponse\\(NEW_ADDR:(.*)\\)$";

	@Autowired
	private SenderService pecSvc;

	@Autowired
	private MessageUtil msgUtils;

	@Autowired
	private PnExtChnFileTransferService fileTransferService;

	@Autowired
	private S3Properties s3Properties;

	private byte[] attachmentExample;

	@Override
	public void savePaperMessage(PnExtChnPaperEvent paperNotification) {
		log.info("PnExtChnServiceFakeImpl - savePaperMessage - START");
		try {
			PnExtChnProgressStatusEvent out = computeResponse(paperNotification);
			Map<String, Object> headers = headersToMap(out.getHeader());
			out = setAttachments(out, s3Properties.getPhysicalDestination());
			statusQueueMessagingTemplate.convertAndSend(statusMessageQueue, out.getPayload(), headers);
		}
		catch ( RuntimeException exc ) {
			log.error("PnExtChnServiceFakeImpl - savePaperMessage - pushError", exc);
		}
		log.info("PnExtChnServiceFakeImpl - savePaperMessage - END");
	}
	@Override
	public void saveDigitalMessage(PnExtChnPecEvent digitalNotification) {
		log.info("PnExtChnServiceFakeImpl - saveDigitalMessage - START");

		if( digitalNotification.getPayload().getPecAddress().endsWith(".real") ) {
			realSend(digitalNotification);
		}
		else {
			fakeSend(digitalNotification);
		}

		log.info("PnExtChnServiceFakeImpl - saveDigitalMessage - END");
	}

	private void realSend(PnExtChnPecEvent notificaDigitale) {
		String iun = notificaDigitale.getPayload().getIun();

		String pecAddress = notificaDigitale.getPayload().getPecAddress().replaceFirst("\\.real$", "");

		String content = msgUtils.prepareMessage( notificaDigitale, MessageBodyType.PLAIN_TEXT );
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
				out = buildResponse(notificaDigitale.getHeader(), notificaDigitale.getPayload().getRequestCorrelationId(),
						"PEC", PnExtChnProgressStatus.OK, null);
				Map<String, Object> headers = headersToMap(out.getHeader());
				log.info("PnExtChnServiceFakeImpl - saveDigitalMessage - before push ok");
				out = setAttachments(out, s3Properties.getDigitalDestination());
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
				out = setAttachments(out, s3Properties.getDigitalDestination());
				statusQueueMessagingTemplate.convertAndSend(statusMessageQueue, out.getPayload(), headers);
			}
			catch ( RuntimeException exc ) {
				log.error("PnExtChnServiceFakeImpl - saveDigitalMessage - pushError", exc);
			}
			log.info("PnExtChnServiceFakeImpl - saveDigitalMessage - failed");
		}
	}

	private PnExtChnProgressStatusEvent setAttachments(PnExtChnProgressStatusEvent evt, String folder) {
		if(evt != null && evt.getPayload() != null &&
				PnExtChnProgressStatus.OK.equals(evt.getPayload().getStatusCode())) {
			try {
				long ts = new Date().getTime();
				if (attachmentExample == null)
					attachmentExample = this.getClass().getClassLoader()
							.getResourceAsStream("attachment_example.pdf")	.readAllBytes();
				String fileName = folder + "attachment_" + ts + ".pdf";
				fileTransferService.transferAttachment(attachmentExample, fileName);
				evt = evt.toBuilder()
						.payload(evt.getPayload().toBuilder()
								.attachmentKeys(Arrays.asList(fileName))
								.build()
						).build();
			} catch (Exception e) {
				log.warn("Could not load attachment example", e);
			}
		}
		return evt;
	}

	private PnExtChnProgressStatusEvent computeResponse(PnExtChnPecEvent evt) {
		PnExtChnProgressStatus outcome = decideOutcome( evt );
		return outcome != null ? buildResponse( evt.getHeader(), evt.getPayload().getRequestCorrelationId(),
				"PEC", outcome, null) : null;
	}

	private PnExtChnProgressStatusEvent computeResponse(PnExtChnPaperEvent evt) {
		PnExtChnProgressStatus outcome = decideOutcome( evt );
		PhysicalAddress addr = null;
		if (PnExtChnProgressStatus.RETRYABLE_FAIL == outcome) {
			Matcher matcher = Pattern.compile(IMMEDIATE_RESPONSE_NEW_ADDR_REGEX)
					.matcher(evt.getPayload().getDestinationAddress().getAddress());
			if (matcher.find()) {
				String newAddr = matcher.group(1);
				addr = evt.getPayload().getDestinationAddress().toBuilder()
						.address(newAddr != null ? newAddr.trim() : "")
						.build();
			}
		}
		PnExtChnProgressStatusEvent statusEvent = buildResponse(evt.getHeader(), evt.getPayload().getRequestCorrelationId(),
				"PAPER", outcome, addr);
		return outcome != null ? statusEvent : null;
	}

	private PnExtChnProgressStatusEvent buildResponse(StandardEventHeader header, String correlationId,
													  String channel, PnExtChnProgressStatus status, PhysicalAddress addr) {
		return PnExtChnProgressStatusEvent.builder()
				.header( StandardEventHeader.builder()
						.eventId( header.getEventId() + "_response" )
						.iun( header.getIun() )
						.eventType( "PEC".equals(channel) ?
								EventType.SEND_PEC_RESPONSE.name() :
								EventType.SEND_PAPER_RESPONSE.name() )
						.createdAt( Instant.now() )
						.publisher( EventPublisher.EXTERNAL_CHANNELS.name() )
						.build()
				)
				.payload(PnExtChnProgressStatusEventPayload.builder()
						.canale(channel)
						.requestCorrelationId( correlationId )
						.iun( header.getIun() )
						.statusCode( status )
						.statusDate( Instant.now() )
						.newPhysicalAddress(addr)
						.build()
				)
				.build();
	}

	protected PnExtChnProgressStatus decideOutcome( PnExtChnPecEvent evt ) {
		PnExtChnProgressStatus status = null;

		String eventId = evt.getHeader().getEventId();
		String retryNumberPart = eventId.replaceFirst(".*([0-9]+)$", "$1");

		String pecAddress = evt.getPayload().getPecAddress();
		if( pecAddress != null ) {

			if (pecAddress.endsWith(".real"))
				return PnExtChnProgressStatus.OK;

			String domainPart = pecAddress.replaceFirst(".*@", "");

			if( domainPart.startsWith("fail-both")
					|| (domainPart.startsWith("fail-first") && "1".equals( retryNumberPart )) ) {
				status = PnExtChnProgressStatus.RETRYABLE_FAIL;
			}
			else if( domainPart.startsWith("do-not-exists") ) {
				status = PnExtChnProgressStatus.PERMANENT_FAIL;
			}
			else if (domainPart.startsWith("works") || domainPart.startsWith("fail-first")){
				status = PnExtChnProgressStatus.OK;
			}
		}

		return status;
	}

	protected PnExtChnProgressStatus decideOutcome( PnExtChnPaperEvent evt ) {
		PnExtChnProgressStatus status = null;

		PhysicalAddress destAddr = evt.getPayload().getDestinationAddress();
		if( destAddr != null ) {
			String addr = destAddr.getAddress() != null ? destAddr.getAddress() : "";

			if( addr.matches(IMMEDIATE_RESPONSE_NEW_ADDR_REGEX) ) {
				status = PnExtChnProgressStatus.RETRYABLE_FAIL;
			}
			else if( addr.startsWith("ImmediateResponse(FAIL)") ) {
				status = PnExtChnProgressStatus.PERMANENT_FAIL;
			}
			else if( addr.startsWith("ImmediateResponse(OK)") ) {
				status = PnExtChnProgressStatus.OK;
			}
		}

		return status;
	}

}
