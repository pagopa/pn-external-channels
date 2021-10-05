package it.pagopa.pn.externalchannels.service;

import com.amazonaws.services.sqs.AmazonSQSAsync;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.awspring.cloud.core.env.ResourceIdResolver;
import io.awspring.cloud.messaging.core.QueueMessagingTemplate;
import it.pagopa.pn.api.dto.events.GenericEvent;
import it.pagopa.pn.api.dto.events.StandardEventHeader;
import it.pagopa.pn.externalchannels.binding.PnExtChnProcessor;
import it.pagopa.pn.externalchannels.util.Util;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.Map;

import static it.pagopa.pn.api.dto.events.StandardEventHeader.*;

@Service
public class EventSenderService {

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private AmazonSQSAsync sqsClient;

    private QueueMessagingTemplate queueMessagingTemplate;

    @PostConstruct
    private void initMessageTemplate() {
        MappingJackson2MessageConverter jacksonMessageConverter =
                new MappingJackson2MessageConverter();
        jacksonMessageConverter.setSerializedPayloadClass(String.class);
        jacksonMessageConverter.setObjectMapper(objectMapper);
        jacksonMessageConverter.setStrictContentTypeMatch(false);
        queueMessagingTemplate = new QueueMessagingTemplate(sqsClient, (ResourceIdResolver) null, jacksonMessageConverter);
    }

    public void sendTo(String queueName, GenericEvent evt) {
        queueMessagingTemplate.convertAndSend( queueName,
                                               evt.getPayload(), headersToMap( evt.getHeader()) );
    }


    Map<String, Object> headersToMap(StandardEventHeader header) {
        Map<String, Object> map = new HashMap<>();
        map.put(PN_EVENT_HEADER_IUN, header.getIun());
        map.put(PN_EVENT_HEADER_EVENT_ID, header.getEventId());
        map.put(PN_EVENT_HEADER_EVENT_TYPE, header.getEventType());
        map.put(PN_EVENT_HEADER_CREATED_AT, Util.formatInstant(header.getCreatedAt()));
        map.put(PN_EVENT_HEADER_PUBLISHER, header.getPublisher());
        return map;
    }
}
