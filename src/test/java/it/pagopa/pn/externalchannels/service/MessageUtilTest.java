package it.pagopa.pn.externalchannels.service;

import freemarker.template.Configuration;
import freemarker.template.TemplateException;
import it.pagopa.pn.api.dto.events.PnExtChnEmailEvent;
import it.pagopa.pn.api.dto.events.PnExtChnEmailEventPayload;
import it.pagopa.pn.api.dto.events.PnExtChnPecEvent;
import it.pagopa.pn.api.dto.events.PnExtChnPecEventPayload;
import it.pagopa.pn.externalchannels.util.MessageUtil;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.ui.freemarker.FreeMarkerConfigurationFactory;

import java.io.IOException;
import java.time.Instant;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@ContextConfiguration(classes = { MessageUtilTest.SpringTestConfiguration.class, MessageUtil.class })
class MessageUtilTest {

	@TestConfiguration
	@TestPropertySource("classpath:application.yaml")
	static class SpringTestConfiguration {
		@Bean
		Configuration freeMarker() throws TemplateException, IOException {
			FreeMarkerConfigurationFactory freeMarkerConfigurationFactory = new FreeMarkerConfigurationFactory();
			return freeMarkerConfigurationFactory.createConfiguration();
		}
	}

	@Autowired
	private MessageUtil util;
	
	private final String IUN = "IUN_01";
	private final String RECIPIENT_DENOMINATION = "Nome Cognome/Ragione Sociale";
	private final String SENDER_DENOMINATION = "PA_01";
	private final Instant SHIPMENT_DATE = Instant.parse("2021-10-05T15:00:00.000Z");
	
	@Test
	void successPecToHtmlBody() {
		//GIVEN
		PnExtChnPecEvent evt = PnExtChnPecEvent.builder().payload(PnExtChnPecEventPayload.builder()
				.iun(IUN)
				.recipientDenomination(RECIPIENT_DENOMINATION)
				.senderDenomination(SENDER_DENOMINATION)
				.shipmentDate(SHIPMENT_DATE)
				.build()
		).build();

		//WHEN
		String message = util.prepareMessage(evt, MessageBodyType.HTML );
		
		//THEN
		Assertions.assertTrue(message.contains( IUN ) );
		Assertions.assertTrue(message.contains( RECIPIENT_DENOMINATION ) );
		Assertions.assertTrue(message.contains( SENDER_DENOMINATION ) );
		Assertions.assertTrue(message.contains( "05-10-2021" ) );
	}
	
	@Test
	void successPecToPlainTextBody() {
		//GIVEN
		PnExtChnPecEvent evt = PnExtChnPecEvent.builder().payload(PnExtChnPecEventPayload.builder()
				.iun(IUN)
				.recipientDenomination(RECIPIENT_DENOMINATION)
				.senderDenomination(SENDER_DENOMINATION)
				.shipmentDate(SHIPMENT_DATE)
				.build()
		).build();

		//WHEN
		String message = util.prepareMessage(evt, MessageBodyType.PLAIN_TEXT );
		
		//THEN
		Assertions.assertTrue(message.contains( IUN ) );
		Assertions.assertTrue(message.contains( RECIPIENT_DENOMINATION ) );
		Assertions.assertTrue(message.contains( SENDER_DENOMINATION ) );
		Assertions.assertTrue(message.contains( "05-10-2021" ) );
	}
	
	@Test
	void successEmailToHtmlBody() {
		//GIVEN
		PnExtChnEmailEvent evt = PnExtChnEmailEvent.builder().payload(PnExtChnEmailEventPayload.builder()
				.iun(IUN)
				.recipientDenomination(RECIPIENT_DENOMINATION)
				.senderDenomination(SENDER_DENOMINATION)
				.shipmentDate(SHIPMENT_DATE)
				.build()
		).build();

		//WHEN
		String message = util.prepareMessage(evt, MessageBodyType.HTML );
		
		//THEN
		Assertions.assertTrue(message.contains( IUN ) );
		Assertions.assertTrue(message.contains( RECIPIENT_DENOMINATION ) );
		Assertions.assertTrue(message.contains( SENDER_DENOMINATION ) );
		Assertions.assertTrue(message.contains( "05-10-2021" ) );
	}
	
	@Test
	void successEmailToPlainTextBody() {
		//GIVEN
		PnExtChnEmailEvent evt = PnExtChnEmailEvent.builder().payload(PnExtChnEmailEventPayload.builder()
				.iun(IUN)
				.recipientDenomination(RECIPIENT_DENOMINATION)
				.senderDenomination(SENDER_DENOMINATION)
				.shipmentDate(SHIPMENT_DATE)
				.build()
		).build();

		//WHEN
		String message = util.prepareMessage(evt, MessageBodyType.PLAIN_TEXT );
		
		//THEN
		Assertions.assertTrue(message.contains( IUN ) );
		Assertions.assertTrue(message.contains( RECIPIENT_DENOMINATION ) );
		Assertions.assertTrue(message.contains( SENDER_DENOMINATION ) );
		Assertions.assertTrue(message.contains( "05-10-2021" ) );
	}
	
}
