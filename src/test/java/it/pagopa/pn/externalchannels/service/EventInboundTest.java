package it.pagopa.pn.externalchannels.service;

import it.pagopa.pn.api.dto.events.MessageType;
import it.pagopa.pn.api.dto.events.PnExtChnPecEventPayload;
import it.pagopa.pn.api.dto.events.PnExtChnProgressStatus;
import it.pagopa.pn.externalchannels.binding.PnExtChnProcessor;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.cassandra.CassandraAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.messaging.SubscribableChannel;
import org.springframework.messaging.support.GenericMessage;
import org.springframework.test.context.ContextConfiguration;

import java.time.Instant;
import java.util.HashMap;

import static it.pagopa.pn.api.dto.events.StandardEventHeader.*;
import static it.pagopa.pn.externalchannels.service.TestUtils.mockPecMessage;
import static it.pagopa.pn.externalchannels.service.TestUtils.toJson;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;

@SpringBootTest
@EnableAutoConfiguration(exclude = {CassandraAutoConfiguration.class})
@ContextConfiguration(classes = {
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
    UnknownEventInboundService unknownEventInboundService;

    @MockBean
    PnExtChnService pnExtChnService;

    @MockBean
    PnExtChnFileTransferService fileTransferService;

    @Test
    void shouldInterceptNonCompliantMessage() {

        HashMap<String, Object> headers = new HashMap<>();
        GenericMessage<String> message = new GenericMessage<>("hello", headers);
        SubscribableChannel channel = processor.notifPecInput();
        assertThrows(Exception.class, () -> channel.send(message));

        verify(unknownEventInboundService, Mockito.times(1))
                .handleUnknownInboundEvent(any(), any());

        verify(pnExtChnPecEventInboundService, Mockito.times(0))
                .handlePnExtChnPecEvent(any(), any(), any(), any(), any(), any());

    }

    @Test
    void shouldInterceptElaborationResultEvent() {

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
    void shouldInterceptEmailMessage() {

        HashMap<String, Object> headers = new HashMap<>();
        headers.put(PN_EVENT_HEADER_EVENT_TYPE, MessageType.PN_EXT_CHN_EMAIL);
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

        verify(pnExtChnEmailEventInboundService, Mockito.times(1))
                .handlePnExtChnEmailEvent(any(), any(), any(), any(), any(), any());

    }

    @Test
    void shouldInterceptPecMessage() {

        HashMap<String, Object> headers = new HashMap<>();
        headers.put(PN_EVENT_HEADER_EVENT_TYPE, MessageType.PN_EXT_CHN_PEC);
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
        headers.put(PN_EVENT_HEADER_EVENT_TYPE, MessageType.PN_EXT_CHN_PEC);
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
        headers.put(PN_EVENT_HEADER_EVENT_TYPE, MessageType.PN_EXT_CHN_PEC);
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
            .produceStatusMessage(any(), any(), eq(MessageType.PN_EXT_CHN_PEC),
                    eq(PnExtChnProgressStatus.PERMANENT_FAIL), any(), anyInt(), any(), any());

        verify(pnExtChnService).discardMessage(any(), any());
    }

}