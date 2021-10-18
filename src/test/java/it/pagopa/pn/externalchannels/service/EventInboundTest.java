package it.pagopa.pn.externalchannels.service;

import it.pagopa.pn.api.dto.events.*;
import it.pagopa.pn.api.dto.notification.address.PhysicalAddress;
import it.pagopa.pn.externalchannels.binding.PnExtChnProcessor;
import it.pagopa.pn.externalchannels.config.properties.EmailProperties;
import it.pagopa.pn.externalchannels.event.elaborationresult.PnExtChnElaborationResultEvent;
import it.pagopa.pn.externalchannels.service.pnextchnservice.PnExtChnService;
import it.pagopa.pn.externalchannels.service.pnextchnservice.PnExtChnServiceSelectorProxy;
import it.pagopa.pn.externalchannels.util.MessageUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.cassandra.CassandraAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.messaging.SubscribableChannel;
import org.springframework.messaging.support.GenericMessage;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.io.IOException;
import java.time.Instant;
import java.util.HashMap;

import static it.pagopa.pn.api.dto.events.StandardEventHeader.*;
import static it.pagopa.pn.externalchannels.service.TestUtils.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@SpringBootTest
@EnableAutoConfiguration(exclude = {CassandraAutoConfiguration.class})
@ContextConfiguration(classes = {
        MessageUtil.class,
        PnExtChnPaperEventInboundService.class,
        PnExtChnPecEventInboundService.class,
        UnknownEventInboundService.class,
        PnExtChnEmailEventInboundService.class,
        PnExtChnElaborationResultInboundService.class
})
@EnableBinding({PnExtChnProcessor.class})
class EventInboundTest {

    @TestConfiguration
    @TestPropertySource(locations = {"classpath:application.yaml", "classpath:application-test.yaml"})
    static class SpringTestConfiguration {

        @Bean
        JavaMailSender javaMailSender(){
            JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
            return mailSender;
        }

        @Bean
        @Primary
        PnExtChnService pnExtChnService(){
            return mock(PnExtChnService.class);
        }

    }

    @Autowired
    PnExtChnProcessor processor;

    @SpyBean
    @Autowired
    JavaMailSender javaMailSender;

    @SpyBean
    @Autowired
    PnExtChnElaborationResultInboundService pnExtChnElaborationResultInboundService;

    @SpyBean
    @Autowired
    PnExtChnEmailEventInboundService pnExtChnEmailEventInboundService;

    @SpyBean
    @Autowired
    PnExtChnPecEventInboundService pnExtChnPecEventInboundService;

    @SpyBean
    @Autowired
    PnExtChnPaperEventInboundService pnExtChnPaperEventInboundService;

    @SpyBean
    @Autowired
    UnknownEventInboundService unknownEventInboundService;

    @Autowired // mocked in configuration
    PnExtChnService pnExtChnService;

    @MockBean
    PnExtChnServiceSelectorProxy pnExtChnServiceSelectorProxy;

    @MockBean
    EmailProperties emailProperties;

    @MockBean
    CsvService csvService;

    @MockBean
    PnExtChnFileTransferService fileTransferService;

    @MockBean
    EventSenderService eventSenderService;

    @BeforeEach
    void init (){
        when(emailProperties.getContentType()).thenReturn(MessageBodyType.HTML);
        doNothing().when(javaMailSender).send((MimeMessage) any());
    }

    @AfterEach
    void clear (){
        clearInvocations(pnExtChnService);
    }

    @Test
    void shouldInterceptNonCompliantMessage() {

        HashMap<String, Object> headers = new HashMap<>();
        GenericMessage<String> message = new GenericMessage<>("hello", headers);
        SubscribableChannel channel = processor.notifPecInput();
        channel.send(message);
        verify(unknownEventInboundService, Mockito.times(1))
                .handleUnknownInboundEvent(any(), any());

        verify(pnExtChnPecEventInboundService, Mockito.times(0))
                .handlePnExtChnPecEvent(any(), any(), any(), any(), any(), any());

    }

    @Test
    void shouldInterceptElaborationResultEvent() throws IOException {

        /*HashMap<String, Object> headers = new HashMap<>();
        String payload = "{}";
        GenericMessage<String> message = new GenericMessage<>(payload, headers);
        SubscribableChannel channel = processor.elabResultInput();
        channel.send(message);*/

        // Force test because StreamListener is currently disabled
        pnExtChnElaborationResultInboundService.handleElaborationResult(new PnExtChnElaborationResultEvent());

        verify(unknownEventInboundService, Mockito.times(0))
                .handleUnknownInboundEvent(any(), any());

        verify(pnExtChnElaborationResultInboundService, Mockito.times(1))
                .handleElaborationResult(any());

    }

    @Test
    void shouldInterceptAndSaveEmailMessage() throws MessagingException {

        when(emailProperties.getUsername()).thenReturn("abc@abc.it");
        when(emailProperties.getPassword()).thenReturn("123");

        HashMap<String, Object> headers = new HashMap<>();
        headers.put(PN_EVENT_HEADER_EVENT_TYPE, EventType.SEND_COURTESY_EMAIL.name());
        headers.put(PN_EVENT_HEADER_EVENT_ID, "123");
        headers.put(PN_EVENT_HEADER_PUBLISHER, "pub");
        headers.put(PN_EVENT_HEADER_IUN, "iun");
        headers.put(PN_EVENT_HEADER_CREATED_AT, Instant.now().toString());
        String payload = toJson(mockEmailMessage().getPayload());
        GenericMessage<String> message = new GenericMessage<>(payload, headers);
        SubscribableChannel channel = processor.notifPecInput();
        channel.send(message);

        verify(unknownEventInboundService, Mockito.times(0))
                .handleUnknownInboundEvent(any(), any());

        verify(pnExtChnEmailEventInboundService, Mockito.times(1))
                .handlePnExtChnEmailEvent(any(), any(), any(), any(), any(), any());

    }

    @Test
    void shouldInterceptAndDiscardEmailMessage() throws MessagingException {

        HashMap<String, Object> headers = new HashMap<>();
        headers.put(PN_EVENT_HEADER_EVENT_TYPE, EventType.SEND_COURTESY_EMAIL.name());
        headers.put(PN_EVENT_HEADER_EVENT_ID, "123");
        headers.put(PN_EVENT_HEADER_PUBLISHER, "pub");
        headers.put(PN_EVENT_HEADER_IUN, "iun");
        headers.put(PN_EVENT_HEADER_CREATED_AT, Instant.now().toString());
        PnExtChnEmailEventPayload evt = mockEmailMessage().getPayload();
        evt = evt.toBuilder().iun(null).build(); // should discard
        String payload = toJson(evt);
        GenericMessage<String> message = new GenericMessage<>(payload, headers);
        SubscribableChannel channel = processor.notifPecInput();
        channel.send(message);

        verify(unknownEventInboundService, Mockito.times(0))
                .handleUnknownInboundEvent(any(), any());

        verify(pnExtChnEmailEventInboundService, Mockito.times(1))
                .handlePnExtChnEmailEvent(any(), any(), any(), any(), any(), any());

        verify(pnExtChnService).discardMessage(any(), any());
    }

    @Test
    void shouldInterceptPecMessage() {

        HashMap<String, Object> headers = new HashMap<>();
        headers.put(PN_EVENT_HEADER_EVENT_TYPE, EventType.SEND_PEC_REQUEST.name());
        headers.put(PN_EVENT_HEADER_EVENT_ID, "123");
        headers.put(PN_EVENT_HEADER_PUBLISHER, "pub");
        headers.put(PN_EVENT_HEADER_IUN, "iun");
        headers.put(PN_EVENT_HEADER_CREATED_AT, Instant.now().toString());
        PnExtChnPecEventPayload evt = mockPecMessage().getPayload();
        evt.setIun(null); // should discard
        String payload = toJson(evt);
        GenericMessage<String> message = new GenericMessage<>(payload, headers);
        SubscribableChannel channel = processor.notifPecInput();
        channel.send(message);

        verify(unknownEventInboundService, Mockito.times(0))
                .handleUnknownInboundEvent(any(), any());

        verify(pnExtChnPecEventInboundService, Mockito.times(1))
                .handlePnExtChnPecEvent(any(), any(), any(), any(), any(), any());

    }

    @Test
    void shouldInterceptAndSavePecMessage() {

        HashMap<String, Object> headers = new HashMap<>();
        headers.put(PN_EVENT_HEADER_EVENT_TYPE, EventType.SEND_PEC_REQUEST.name());
        headers.put(PN_EVENT_HEADER_EVENT_ID, "123");
        headers.put(PN_EVENT_HEADER_PUBLISHER, "pub");
        headers.put(PN_EVENT_HEADER_IUN, "iun");
        headers.put(PN_EVENT_HEADER_CREATED_AT, Instant.now().toString());
        String payload = toJson(mockPecMessage().getPayload());
        GenericMessage<String> message = new GenericMessage<>(payload, headers);
        SubscribableChannel channel = processor.notifPecInput();
        channel.send(message);

        verify(pnExtChnPecEventInboundService, Mockito.times(1))
                .handlePnExtChnPecEvent(any(), any(), any(), any(), any(), any());

        verify(pnExtChnServiceSelectorProxy)
                .saveDigitalMessage(any());

    }

    @Test
    void shouldInterceptAndDiscardPecMessage() {

        HashMap<String, Object> headers = new HashMap<>();
        headers.put(PN_EVENT_HEADER_EVENT_TYPE, EventType.SEND_PEC_REQUEST.name());
        headers.put(PN_EVENT_HEADER_EVENT_ID, "123");
        headers.put(PN_EVENT_HEADER_PUBLISHER, "pub");
        headers.put(PN_EVENT_HEADER_IUN, "iun");
        headers.put(PN_EVENT_HEADER_CREATED_AT, Instant.now().toString());
        PnExtChnPecEventPayload pecEvent = mockPecMessage().getPayload();
        pecEvent.setIun(null); // should discard
        String payload = toJson(pecEvent);
        GenericMessage<String> message = new GenericMessage<>(payload, headers);
        SubscribableChannel channel = processor.notifPecInput();
        channel.send(message);

        verify(pnExtChnPecEventInboundService, Mockito.times(1))
                .handlePnExtChnPecEvent(any(), any(), any(), any(), any(), any());

        verify(pnExtChnServiceSelectorProxy, Mockito.times(1))
            .produceStatusMessage(any(), any(), eq(EventType.SEND_PEC_RESPONSE),
                    eq(PnExtChnProgressStatus.PERMANENT_FAIL), any(), anyInt(), any(), any());

        verify(pnExtChnServiceSelectorProxy).discardMessage(any(), any());
    }

    @Test
    void shouldInterceptPaperMessage() {

        HashMap<String, Object> headers = new HashMap<>();
        headers.put(PN_EVENT_HEADER_EVENT_TYPE, EventType.SEND_PAPER_REQUEST.name());
        headers.put(PN_EVENT_HEADER_EVENT_ID, "123");
        headers.put(PN_EVENT_HEADER_PUBLISHER, "pub");
        headers.put(PN_EVENT_HEADER_IUN, "iun");
        headers.put(PN_EVENT_HEADER_CREATED_AT, Instant.now().toString());
        String payload = toJson(mockPaperMessage().getPayload());
        GenericMessage<String> message = new GenericMessage<>(payload, headers);
        SubscribableChannel channel = processor.notifPecInput();
        channel.send(message);

        verify(unknownEventInboundService, Mockito.times(0))
                .handleUnknownInboundEvent(any(), any());

        verify(pnExtChnPaperEventInboundService, Mockito.times(1))
                .handlePnExtChnPaperEvent(any(), any(), any(), any(), any(), any());

    }

    @Test
    void shouldInterceptAndSavePaperMessage() {

        HashMap<String, Object> headers = new HashMap<>();
        headers.put(PN_EVENT_HEADER_EVENT_TYPE, EventType.SEND_PAPER_REQUEST.name());
        headers.put(PN_EVENT_HEADER_EVENT_ID, "123");
        headers.put(PN_EVENT_HEADER_PUBLISHER, "pub");
        headers.put(PN_EVENT_HEADER_IUN, "iun");
        headers.put(PN_EVENT_HEADER_CREATED_AT, Instant.now().toString());
        String payload = toJson(mockPaperMessage().getPayload());
        GenericMessage<String> message = new GenericMessage<>(payload, headers);
        SubscribableChannel channel = processor.notifPecInput();
        channel.send(message);

        verify(pnExtChnPaperEventInboundService, Mockito.times(1))
                .handlePnExtChnPaperEvent(any(), any(), any(), any(), any(), any());

        verify(pnExtChnServiceSelectorProxy)
                .savePaperMessage(any());

    }

    @Test
    void shouldInterceptAndDiscardPaperMessage() {

        HashMap<String, Object> headers = new HashMap<>();
        headers.put(PN_EVENT_HEADER_EVENT_TYPE, EventType.SEND_PAPER_REQUEST.name());
        headers.put(PN_EVENT_HEADER_EVENT_ID, "123");
        headers.put(PN_EVENT_HEADER_PUBLISHER, "pub");
        headers.put(PN_EVENT_HEADER_IUN, "iun");
        headers.put(PN_EVENT_HEADER_CREATED_AT, Instant.now().toString());
        PnExtChnPaperEventPayload pecEvent = mockPaperMessage().getPayload();
        pecEvent.setIun(null); // should discard
        String payload = toJson(pecEvent);
        GenericMessage<String> message = new GenericMessage<>(payload, headers);
        SubscribableChannel channel = processor.notifPecInput();
        channel.send(message);

        verify(pnExtChnPaperEventInboundService, Mockito.times(1))
                .handlePnExtChnPaperEvent(any(), any(), any(), any(), any(), any());

        verify(pnExtChnServiceSelectorProxy, Mockito.times(1))
                .produceStatusMessage(any(), any(), eq(EventType.SEND_PAPER_RESPONSE),
                        eq(PnExtChnProgressStatus.PERMANENT_FAIL), any(), anyInt(), any(), any());

        verify(pnExtChnServiceSelectorProxy).discardMessage(any(), any());
    }

}