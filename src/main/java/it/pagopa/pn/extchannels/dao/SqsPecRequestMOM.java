package it.pagopa.pn.extchannels.dao;

import com.fasterxml.jackson.databind.ObjectMapper;
import it.pagopa.pn.api.dto.events.PnExtChnPecEvent;
import it.pagopa.pn.commons.mom.sqs.GenericSqsMOM;
import software.amazon.awssdk.services.sqs.SqsClient;


public class SqsPecRequestMOM extends GenericSqsMOM<PnExtChnPecEvent> implements PecRequestMOM {

    public SqsPecRequestMOM(SqsClient sqs, ObjectMapper objMapper, String queueName) {
        super( sqs, objMapper, PnExtChnPecEvent.class, queueName );
    }

}
