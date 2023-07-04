package it.pagopa.pn.externalchannels.sqs.producer;

import com.fasterxml.jackson.databind.ObjectMapper;
import it.pagopa.pn.api.dto.events.MomProducer;
import it.pagopa.pn.externalchannels.event.InternalEvent;
import software.amazon.awssdk.services.sqs.SqsClient;


public class InternalProducer extends AbstractDelaySqsMomProducer<InternalEvent> implements MomProducer<InternalEvent> {

    public InternalProducer(SqsClient sqsClient, String topic, ObjectMapper objectMapper) {
        super(sqsClient, topic, objectMapper, InternalEvent.class);
    }
}
