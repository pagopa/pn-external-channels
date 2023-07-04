package it.pagopa.pn.externalchannels.sqs.producer;

import com.fasterxml.jackson.databind.ObjectMapper;
import it.pagopa.pn.api.dto.events.MomProducer;
import it.pagopa.pn.api.dto.events.AbstractSqsMomProducer;
import it.pagopa.pn.externalchannels.event.PnDeliveryPushEvent;
import software.amazon.awssdk.services.sqs.SqsClient;

public class DeliveryPushProducer extends AbstractSqsMomProducer<PnDeliveryPushEvent> implements MomProducer<PnDeliveryPushEvent> {


    public DeliveryPushProducer(SqsClient sqsClient, String topic, ObjectMapper objectMapper) {
        super(sqsClient, topic, objectMapper, PnDeliveryPushEvent.class);
    }
}
