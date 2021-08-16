package it.pagopa.pn.extchannels.dao;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.services.sqs.SqsClient;

@Configuration
public class DaoFactory {

    @Value("${pn.ext-channel.topics.input}")
    private String inputQueueName;

    @Value("${pn.ext-channel.topics.output}")
    private String outputQueueName;

    @ConditionalOnProperty( name="pn.mom", havingValue = "sqs")
    @Bean
    public PecRequestMOM pecRequestMOM(SqsClient sqs, ObjectMapper objMapper) {
        return new SqsPecRequestMOM( sqs, objMapper, inputQueueName );
    }

    @ConditionalOnProperty( name="pn.mom", havingValue = "sqs")
    @Bean
    public PecResponseMOM pecResponseMOM(SqsClient sqs, ObjectMapper objMapper) {
        return new SqsPecResponseMOM( sqs, objMapper, outputQueueName );
    }

}
