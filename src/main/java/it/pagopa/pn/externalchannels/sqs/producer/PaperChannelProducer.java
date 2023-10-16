package it.pagopa.pn.externalchannels.sqs.producer;

import com.fasterxml.jackson.databind.ObjectMapper;
import it.pagopa.pn.api.dto.events.AbstractSqsMomProducer;
import it.pagopa.pn.api.dto.events.MomProducer;
import it.pagopa.pn.externalchannels.event.PaperChannelEvent;
import software.amazon.awssdk.services.sqs.SqsClient;

public class PaperChannelProducer extends AbstractSqsMomProducer<PaperChannelEvent> implements MomProducer<PaperChannelEvent> {


    public PaperChannelProducer(SqsClient sqsClient, String topic, ObjectMapper objectMapper) {
        super(sqsClient, topic, objectMapper, PaperChannelEvent.class);
    }
}
