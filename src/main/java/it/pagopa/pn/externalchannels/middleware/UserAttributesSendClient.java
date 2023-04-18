package it.pagopa.pn.externalchannels.middleware;

import it.pagopa.pn.api.dto.events.MomProducer;
import it.pagopa.pn.externalchannels.event.PnDeliveryPushEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserAttributesSendClient {

    private final MomProducer<PnDeliveryPushEvent> userAttributesProducer;


    public void sendNotification(PnDeliveryPushEvent pnDeliveryPushEvent) {
        userAttributesProducer.push(pnDeliveryPushEvent);
    }
}
