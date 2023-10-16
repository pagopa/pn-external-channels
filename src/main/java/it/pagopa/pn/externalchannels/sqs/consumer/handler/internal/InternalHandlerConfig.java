package it.pagopa.pn.externalchannels.sqs.consumer.handler.internal;

import it.pagopa.pn.externalchannels.dto.NotificationProgress;
import it.pagopa.pn.externalchannels.event.InternalEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;

import java.util.function.Consumer;

@Configuration
@RequiredArgsConstructor
public class InternalHandlerConfig {

    private final InternalEventHandler handler;

    @Bean
    Consumer<Message<NotificationProgress>> internalEventConsumer() {
        return internalEventMessage -> {
            System.out.println("INT: " + internalEventMessage);
            handler.handleMessage(internalEventMessage.getPayload());
        };
    }
}
