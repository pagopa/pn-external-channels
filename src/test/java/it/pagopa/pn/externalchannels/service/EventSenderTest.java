package it.pagopa.pn.externalchannels.service;

import com.amazonaws.services.sqs.AmazonSQSAsync;
import com.amazonaws.services.sqs.AmazonSQSAsyncClientBuilder;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.awspring.cloud.messaging.core.QueueMessagingTemplate;
import it.pagopa.pn.api.dto.events.PnExtChnEmailEventPayload;
import it.pagopa.pn.api.dto.events.PnExtChnPecEventPayload;
import it.pagopa.pn.externalchannels.util.MessageUtil;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedConstruction;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;

import static it.pagopa.pn.externalchannels.service.TestUtils.mockPaperMessage;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@ContextConfiguration(classes = {
		EventSenderTest.SpringTestConfiguration.class,
		EventSenderService.class
})
public class EventSenderTest {


	@TestConfiguration
	@TestPropertySource("classpath:application.yaml")
	static class SpringTestConfiguration {
		@Bean
		ObjectMapper objectMapper(){
			return new ObjectMapper();
		}

		@Bean
		AmazonSQSAsync amazonSQSAsync(){
			return AmazonSQSAsyncClientBuilder.defaultClient();
		}
	}

	@Autowired
	EventSenderService eventSenderService;

	@MockBean
	QueueMessagingTemplate queueMessagingTemplate;
	
	@BeforeEach
    void init() {
		ReflectionTestUtils.setField(eventSenderService, "queueMessagingTemplate", queueMessagingTemplate);
	}

	@Test
	void shouldSendEvent(){
		eventSenderService.sendTo("test", mockPaperMessage());
	}

}
