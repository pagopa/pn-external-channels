package it.pagopa.pn.externalchannels.sqs;

import com.fasterxml.jackson.databind.ObjectMapper;
import it.pagopa.pn.api.dto.events.GenericEvent;
import it.pagopa.pn.commons.abstractions.MomProducer;
import it.pagopa.pn.commons.abstractions.impl.AbstractSqsMomProducer;
import software.amazon.awssdk.services.sqs.SqsClient;

public class DeliveryPushProducer extends AbstractSqsMomProducer<GenericEvent> implements MomProducer<GenericEvent> {


    public DeliveryPushProducer(SqsClient sqsClient, String topic, ObjectMapper objectMapper) {
        super(sqsClient, topic, objectMapper, GenericEvent.class);
    }
}
