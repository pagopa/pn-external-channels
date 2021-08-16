package it.pagopa.pn.commons.mom.sqs;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import it.pagopa.pn.commons.mom.MomConsumer;
import it.pagopa.pn.commons.mom.MomProducer;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.sqs.SqsAsyncClient;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.*;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class GenericSqsMOM<T> implements MomProducer<T>, MomConsumer<T> {

    private final String queueName;
    private final Class<T> bodyClass;
    private final ObjectMapper objMapper;

    private final SqsClient sqs;

    private final String queueUrl;


    public GenericSqsMOM(SqsClient sqs, ObjectMapper objMapper, Class<T> bodyClass, String queueName) {
        this.queueName = queueName;
        this.bodyClass = bodyClass;
        this.sqs = sqs;
        this.objMapper = objMapper;
        queueUrl = getQueueUrl( sqs );
    }

    private String getQueueUrl(SqsClient sqs) {
        GetQueueUrlRequest getQueueRequest = GetQueueUrlRequest.builder()
                .queueName(queueName)
                .build();

        return sqs.getQueueUrl(getQueueRequest).queueUrl();
    }

    @Override
    public void poll(Duration maxPollTime, Consumer<T> handler) {
        long maxPollSeconds = maxPollTime.getSeconds();
        int maxPollSecondsInt = ( maxPollSeconds > 120 ? 120 : (int) maxPollSeconds);

        ReceiveMessageRequest receiveRequest = ReceiveMessageRequest.builder()
                .queueUrl(queueUrl)
                .maxNumberOfMessages( 10 )
                .waitTimeSeconds( maxPollSecondsInt )
                .build();

        for( Message sqsMsg: sqs.receiveMessage(receiveRequest).messages() ) {
            T evt = parseJson( sqsMsg.body() );
            handler.accept( evt );
            deleteMessage( sqsMsg );
        }
    }

    private void deleteMessage(Message awsMsg) {
        DeleteMessageRequest deleteMessageRequest = DeleteMessageRequest.builder()
                .queueUrl(queueUrl)
                .receiptHandle( awsMsg.receiptHandle())
                .build();

        sqs.deleteMessage(deleteMessageRequest);
    }

    private T parseJson(String body) {
        try {
            return objMapper.readValue( body, bodyClass );
        } catch (JsonProcessingException exc) {
            throw new IllegalStateException( exc ); // FIXME Definre trattazione eccezioni
        }
    }

    @Override
    public void push(T msg) {
        String jsonMessage = objToJson_handleExceptions(msg);

        SendMessageRequest sendMsgRequest = SendMessageRequest.builder()
                .queueUrl(queueUrl)
                .messageBody( jsonMessage )
                .build();

        sqs.sendMessage(sendMsgRequest);
    }

    private String objToJson_handleExceptions(T msg) {
        try {
            return objMapper.writeValueAsString(msg);
        } catch (JsonProcessingException exc) {
            throw new IllegalStateException( exc ); // FIXME Definre trattazione eccezioni
        }
    }
}
