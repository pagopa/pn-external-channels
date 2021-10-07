package it.pagopa.pn.externalchannels.service;

import java.time.Instant;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import it.pagopa.pn.api.dto.events.PnExtChnEmailEventPayload;
import it.pagopa.pn.api.dto.events.PnExtChnPecEventPayload;
import it.pagopa.pn.externalchannels.util.MessageUtil;

public class MessageUtilTest {
		
	private MessageUtil util;
	
	private final String IUN = "IUN_01";
	private final String RECIPIENT_DENOMINATION = "Nome Cognome/Ragione Sociale";
	private final String SENDER_DENOMINATION = "PA_01";
	private final Instant SHIPMENT_DATE = Instant.parse("2021-10-05T15:00:00.000Z");
	
	@BeforeEach
    public void setup() {
		util = new MessageUtil();
    }
	
	@Test
	void successPecToHtmlBody() {
		//GIVEN
		PnExtChnPecEventPayload payload = PnExtChnPecEventPayload.builder()
			.iun( IUN )
			.recipientDenomination( RECIPIENT_DENOMINATION )
			.senderDenomination( SENDER_DENOMINATION )
			.shipmentDate( SHIPMENT_DATE )
			.build();
		
		//WHEN
		String message = util.pecPayloadToMessage( payload, MessageBodyType.HTML );
		
		//THEN
		Assertions.assertTrue(message.contains( IUN ) );
		Assertions.assertTrue(message.contains( RECIPIENT_DENOMINATION ) );
		Assertions.assertTrue(message.contains( SENDER_DENOMINATION ) );
		Assertions.assertTrue(message.contains( "05-10-2021" ) );
	}
	
	@Test
	void successPecToPlainTextBody() {
		//GIVEN
		PnExtChnPecEventPayload payload = PnExtChnPecEventPayload.builder()
			.iun( IUN )
			.recipientDenomination( RECIPIENT_DENOMINATION )
			.senderDenomination( SENDER_DENOMINATION )
			.shipmentDate( SHIPMENT_DATE )
			.build();
		
		//WHEN
		String message = util.pecPayloadToMessage( payload, MessageBodyType.PLAIN_TEXT );
		
		//THEN
		Assertions.assertTrue(message.contains( IUN ) );
		Assertions.assertTrue(message.contains( RECIPIENT_DENOMINATION ) );
		Assertions.assertTrue(message.contains( SENDER_DENOMINATION ) );
		Assertions.assertTrue(message.contains( "05-10-2021" ) );
	}
	
	@Test
	void successEmailToHtmlBody() {
		//GIVEN
		PnExtChnEmailEventPayload payload = PnExtChnEmailEventPayload.builder()
			.iun( IUN )
			.recipientDenomination( RECIPIENT_DENOMINATION )
			.senderDenomination( SENDER_DENOMINATION )
			.shipmentDate( SHIPMENT_DATE )
			.build();
		
		//WHEN
		String message = util.mailPayloadToMessage( payload, MessageBodyType.HTML );
		
		//THEN
		Assertions.assertTrue(message.contains( IUN ) );
		Assertions.assertTrue(message.contains( RECIPIENT_DENOMINATION ) );
		Assertions.assertTrue(message.contains( SENDER_DENOMINATION ) );
		Assertions.assertTrue(message.contains( "05-10-2021" ) );
	}
	
	@Test
	void successEmailToPlainTextBody() {
		//GIVEN
		PnExtChnEmailEventPayload payload = PnExtChnEmailEventPayload.builder()
			.iun( IUN )
			.recipientDenomination( RECIPIENT_DENOMINATION )
			.senderDenomination( SENDER_DENOMINATION )
			.shipmentDate( SHIPMENT_DATE )
			.build();
		
		//WHEN
		String message = util.mailPayloadToMessage( payload, MessageBodyType.PLAIN_TEXT );
		
		//THEN
		Assertions.assertTrue(message.contains( IUN ) );
		Assertions.assertTrue(message.contains( RECIPIENT_DENOMINATION ) );
		Assertions.assertTrue(message.contains( SENDER_DENOMINATION ) );
		Assertions.assertTrue(message.contains( "05-10-2021" ) );
	}
	
}
