package it.pagopa.pn.externalchannels.middleware;

import it.pagopa.pn.commons.abstractions.MomProducer;
import it.pagopa.pn.externalchannels.event.PnDeliveryPushCourtesyEvent;
import it.pagopa.pn.externalchannels.event.PnDeliveryPushPaperEvent;
import it.pagopa.pn.externalchannels.event.PnDeliveryPushPecEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DeliveryPushSendClient {

    private final MomProducer<PnDeliveryPushPecEvent> pecProducer;

    private final MomProducer<PnDeliveryPushCourtesyEvent> courtesyProducer;

    private final MomProducer<PnDeliveryPushPaperEvent> paperProducer;

    public void sendNotification(PnDeliveryPushPecEvent pnDeliveryPushPecEvent) {
        pecProducer.push(pnDeliveryPushPecEvent);
    }

    public void sendNotification(PnDeliveryPushCourtesyEvent pnDeliveryPushCourtesyEvent) {
        courtesyProducer.push(pnDeliveryPushCourtesyEvent);
    }

    public void sendNotification(PnDeliveryPushPaperEvent pnDeliveryPushPaperEvent) {
        paperProducer.push(pnDeliveryPushPaperEvent);
    }
}
