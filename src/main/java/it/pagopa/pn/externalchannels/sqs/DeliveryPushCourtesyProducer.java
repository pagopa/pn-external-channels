package it.pagopa.pn.externalchannels.sqs;

import com.fasterxml.jackson.databind.ObjectMapper;
import it.pagopa.pn.commons.abstractions.MomProducer;
import it.pagopa.pn.commons.abstractions.impl.AbstractSqsMomProducer;
import it.pagopa.pn.externalchannels.event.PnDeliveryPushCourtesyEvent;
import software.amazon.awssdk.services.sqs.SqsClient;

public class DeliveryPushCourtesyProducer extends AbstractSqsMomProducer<PnDeliveryPushCourtesyEvent> implements MomProducer<PnDeliveryPushCourtesyEvent> {


    public DeliveryPushCourtesyProducer(SqsClient sqsClient, String topic, ObjectMapper objectMapper) {
        super(sqsClient, topic, objectMapper, PnDeliveryPushCourtesyEvent.class);
    }
}
