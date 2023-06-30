package it.pagopa.pn.externalchannels.middleware;

import it.pagopa.pn.api.dto.events.MomProducer;
import it.pagopa.pn.externalchannels.event.PaperChannelEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PaperChannelSendClient {

    private final MomProducer<PaperChannelEvent> deliveryPushProducer;


    public void sendNotification(PaperChannelEvent paperChannelEvent) {
        deliveryPushProducer.push(paperChannelEvent);
    }
}
