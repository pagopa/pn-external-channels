package it.pagopa.pn.externalchannels.sqs;

import com.fasterxml.jackson.databind.ObjectMapper;
import it.pagopa.pn.commons.abstractions.MomProducer;
import it.pagopa.pn.commons.abstractions.impl.AbstractSqsMomProducer;
import it.pagopa.pn.externalchannels.event.PnDeliveryPushEmailEvent;
import it.pagopa.pn.externalchannels.event.PnDeliveryPushPecEvent;
import software.amazon.awssdk.services.sqs.SqsClient;

public class DeliveryPushEmailProducer extends AbstractSqsMomProducer<PnDeliveryPushEmailEvent> implements MomProducer<PnDeliveryPushEmailEvent> {


    public DeliveryPushEmailProducer(SqsClient sqsClient, String topic, ObjectMapper objectMapper) {
        super(sqsClient, topic, objectMapper, PnDeliveryPushEmailEvent.class);
    }
}
