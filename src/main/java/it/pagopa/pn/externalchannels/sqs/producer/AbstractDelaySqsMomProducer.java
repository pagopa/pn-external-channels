package it.pagopa.pn.externalchannels.sqs.producer;

import com.fasterxml.jackson.databind.ObjectMapper;
import it.pagopa.pn.api.dto.events.AbstractSqsMomProducer;
import it.pagopa.pn.api.dto.exception.SQSSendMessageException;
import it.pagopa.pn.externalchannels.dto.CodeTimeToSend;
import it.pagopa.pn.externalchannels.event.InternalEvent;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.*;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public abstract class AbstractDelaySqsMomProducer<T extends InternalEvent> extends AbstractSqsMomProducer<T> {

    protected AbstractDelaySqsMomProducer(SqsClient sqsClient, String topic, ObjectMapper objectMapper, Class<T> msgClass) {
        super(sqsClient, topic, objectMapper, msgClass);
    }

    //override for delayed messages
    @Override
    public void push(List<T> msges) {
        SendMessageBatchResponse response = this.sqsClient.sendMessageBatch(SendMessageBatchRequest.builder().queueUrl(this.queueUrl).entries(msges.stream().map(msg -> {
            CodeTimeToSend headElement = new LinkedList<>(msg.getPayload().getCodeTimeToSendQueue()).peek();
            return SendMessageBatchRequestEntry.builder().messageBody(this.toJson(msg.getPayload())).id(msg.getHeader().getEventId()).messageAttributes(this.getSqSHeader(msg.getHeader())).delaySeconds((int) headElement.getTime().getSeconds()).build();
        }).toList()).build());
        if (response.hasFailed()) {
            StringBuilder builder = new StringBuilder();
            Iterator var4 = response.failed().iterator();

            while(var4.hasNext()) {
                BatchResultErrorEntry fail = (BatchResultErrorEntry)var4.next();
                builder.append(fail.code());
                builder.append("-");
                builder.append(fail.message());
                builder.append(";");
            }

            throw new SQSSendMessageException(builder.toString());
        }
    }
}
