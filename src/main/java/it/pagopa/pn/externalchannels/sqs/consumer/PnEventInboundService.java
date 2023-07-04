package it.pagopa.pn.externalchannels.sqs.consumer;

import it.pagopa.pn.externalchannels.exception.ExternalChannelsMockException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.function.context.MessageRoutingCallback;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;

import static it.pagopa.pn.externalchannels.event.InternalEvent.INTERNAL_EVENT;

@Configuration
@Slf4j
public class PnEventInboundService {

    @Bean
    public MessageRoutingCallback customRouter() {
        return new MessageRoutingCallback() {
            @Override
            public FunctionRoutingResult routingResult(Message<?> message) {
                return new FunctionRoutingResult(handleMessage(message));
            }
        };
    }

    private String handleMessage(Message<?> message) {
        String eventType = (String) message.getHeaders().get("eventType");
        log.debug("Received message from customRouter with eventType={}", eventType);

        if (INTERNAL_EVENT.equals(eventType)) {
            return "internalEventConsumer";
        } else {
            throw new ExternalChannelsMockException("EventType " + eventType + " not managed");
        }
    }

}
