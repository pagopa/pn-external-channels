package it.pagopa.pn.externalchannels.service;

import it.pagopa.pn.api.dto.events.EventType;
import it.pagopa.pn.api.dto.events.PnExtChnPaperEventPayload;
import it.pagopa.pn.api.dto.events.PnExtChnPecEventPayload;
import it.pagopa.pn.api.dto.events.PnExtChnProgressStatus;
import it.pagopa.pn.externalchannels.binding.PnExtChnProcessor;
import it.pagopa.pn.externalchannels.config.properties.EmailProperties;
import it.pagopa.pn.externalchannels.util.MessageUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.cassandra.CassandraAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.messaging.SubscribableChannel;
import org.springframework.messaging.support.GenericMessage;
import org.springframework.test.context.ContextConfiguration;

import javax.mail.MessagingException;
import java.io.IOException;
import java.time.Instant;
import java.util.HashMap;

import static it.pagopa.pn.api.dto.events.StandardEventHeader.*;
import static it.pagopa.pn.externalchannels.service.TestUtils.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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

    @Autowired
    PnExtChnProcessor processor;

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

    @MockBean
    PnExtChnService pnExtChnService;

    @MockBean
    JavaMailSender javaMailSender;

    @MockBean
    EmailProperties emailProperties;

    @MockBean
    CsvService csvService;

    @MockBean
    PnExtChnFileTransferService fileTransferService;

    @BeforeEach
    void init (){
        when(emailProperties.getContentType()).thenReturn(MessageBodyType.HTML);
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
    @Disabled("Result poller enabled instead")
    void shouldInterceptElaborationResultEvent() throws IOException {

        HashMap<String, Object> headers = new HashMap<>();
        String payload = "{}";
        GenericMessage<String> message = new GenericMessage<>(payload, headers);
        SubscribableChannel channel = processor.elabResultInput();
        channel.send(message);

        verify(unknownEventInboundService, Mockito.times(0))
                .handleUnknownInboundEvent(any(), any());

        verify(pnExtChnElaborationResultInboundService, Mockito.times(1))
                .handleElaborationResult(any());

    }

    @Test
    @Disabled("email mock in place")
    void shouldInterceptEmailMessage() throws MessagingException {

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
    void shouldInterceptPecMessage() {

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

        verify(pnExtChnService)
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

        verify(pnExtChnService, Mockito.times(1))
            .produceStatusMessage(any(), any(), eq(EventType.SEND_PEC_RESPONSE),
                    eq(PnExtChnProgressStatus.PERMANENT_FAIL), any(), anyInt(), any(), any());

        verify(pnExtChnService).discardMessage(any(), any());
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

        verify(pnExtChnService)
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

        verify(pnExtChnService, Mockito.times(1))
                .produceStatusMessage(any(), any(), eq(EventType.SEND_PAPER_RESPONSE),
                        eq(PnExtChnProgressStatus.PERMANENT_FAIL), any(), anyInt(), any(), any());

        verify(pnExtChnService).discardMessage(any(), any());
    }

}