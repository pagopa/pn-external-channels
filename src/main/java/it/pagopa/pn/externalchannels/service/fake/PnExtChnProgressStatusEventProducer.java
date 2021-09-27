package it.pagopa.pn.externalchannels.service.fake;

import com.fasterxml.jackson.databind.ObjectMapper;
import it.pagopa.pn.api.dto.events.PnExtChnProgressStatusEvent;
import it.pagopa.pn.commons.abstractions.impl.AbstractSqsMomProducer;
import it.pagopa.pn.externalchannels.binding.PnExtChnProcessor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.sqs.SqsClient;

@Component
public class PnExtChnProgressStatusEventProducer extends AbstractSqsMomProducer<PnExtChnProgressStatusEvent> {

    protected PnExtChnProgressStatusEventProducer(
            SqsClient sqsClient,
            @Value("${spring.cloud.stream.bindings." + PnExtChnProcessor.STATUS_OUTPUT + ".destination}") String topic,
            ObjectMapper objectMapper
    ) {
        super(sqsClient, topic, objectMapper, PnExtChnProgressStatusEvent.class);
    }
}
