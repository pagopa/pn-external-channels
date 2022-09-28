package it.pagopa.pn.externalchannels.sqs;

import com.fasterxml.jackson.databind.ObjectMapper;
import it.pagopa.pn.commons.abstractions.MomProducer;
import it.pagopa.pn.commons.abstractions.impl.AbstractSqsMomProducer;
import it.pagopa.pn.externalchannels.event.PnDeliveryPushPaperEvent;
import software.amazon.awssdk.services.sqs.SqsClient;

public class DeliveryPushPaperProducer extends AbstractSqsMomProducer<PnDeliveryPushPaperEvent> implements MomProducer<PnDeliveryPushPaperEvent> {


    public DeliveryPushPaperProducer(SqsClient sqsClient, String topic, ObjectMapper objectMapper) {
        super(sqsClient, topic, objectMapper, PnDeliveryPushPaperEvent.class);
    }
}
