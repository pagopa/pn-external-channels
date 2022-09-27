package it.pagopa.pn.externalchannels.sqs;

import com.fasterxml.jackson.databind.ObjectMapper;
import it.pagopa.pn.commons.abstractions.MomProducer;
import it.pagopa.pn.commons.abstractions.impl.AbstractSqsMomProducer;
import it.pagopa.pn.externalchannels.event.PnDeliveryPushPecEvent;
import software.amazon.awssdk.services.sqs.SqsClient;

public class DeliveryPushPecProducer extends AbstractSqsMomProducer<PnDeliveryPushPecEvent> implements MomProducer<PnDeliveryPushPecEvent> {


    public DeliveryPushPecProducer(SqsClient sqsClient, String topic, ObjectMapper objectMapper) {
        super(sqsClient, topic, objectMapper, PnDeliveryPushPecEvent.class);
    }
}
