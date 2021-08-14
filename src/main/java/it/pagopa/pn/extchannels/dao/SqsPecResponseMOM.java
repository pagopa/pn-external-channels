package it.pagopa.pn.extchannels.dao;

import com.fasterxml.jackson.databind.ObjectMapper;
import it.pagopa.pn.api.dto.events.PnExtChnProgressStatusEvent;
import it.pagopa.pn.commons.mom.sqs.GenericSqsMOM;
import software.amazon.awssdk.services.sqs.SqsClient;


public class SqsPecResponseMOM extends GenericSqsMOM<PnExtChnProgressStatusEvent> implements PecResponseMOM {

    public SqsPecResponseMOM(SqsClient sqs, ObjectMapper objMapper, String queueName) {
        super( sqs, objMapper, PnExtChnProgressStatusEvent.class, queueName );
    }

}
