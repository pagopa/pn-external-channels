package it.pagopa.pn.externalchannels.sqs.producer;

import com.fasterxml.jackson.databind.ObjectMapper;
import it.pagopa.pn.api.dto.events.AbstractSqsMomProducer;
import it.pagopa.pn.api.dto.events.MomProducer;
import it.pagopa.pn.externalchannels.event.OcrEvent;
import software.amazon.awssdk.services.sqs.SqsClient;

public class OcrProducer extends AbstractSqsMomProducer<OcrEvent> implements MomProducer<OcrEvent> {


    public OcrProducer(SqsClient sqsClient, String topic, ObjectMapper objectMapper) {
        super(sqsClient, topic, objectMapper, OcrEvent.class);
    }
}
