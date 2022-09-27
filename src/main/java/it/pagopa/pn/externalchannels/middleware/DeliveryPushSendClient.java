package it.pagopa.pn.externalchannels.middleware;

import it.pagopa.pn.commons.abstractions.MomProducer;
import it.pagopa.pn.externalchannels.event.PnDeliveryPushEmailEvent;
import it.pagopa.pn.externalchannels.event.PnDeliveryPushPecEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DeliveryPushSendClient {

    private final MomProducer<PnDeliveryPushPecEvent> pecProducer;

    private final MomProducer<PnDeliveryPushEmailEvent> emailProducer;

    public void sendNotification(PnDeliveryPushPecEvent pnDeliveryPushPecEvent) {
        pecProducer.push(pnDeliveryPushPecEvent);
    }

    public void sendNotification(PnDeliveryPushEmailEvent pnDeliveryPushEmailEvent) {
        emailProducer.push(pnDeliveryPushEmailEvent);
    }
}
