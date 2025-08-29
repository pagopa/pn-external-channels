package it.pagopa.pn.externalchannels.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import it.pagopa.pn.externalchannels.sqs.producer.*;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import software.amazon.awssdk.services.sqs.SqsClient;

@Configuration
@EnableScheduling
@EnableConfigurationProperties
@RequiredArgsConstructor
public class AppConfig {

    private final PnExternalChannelsProperties properties;

    @Bean
    DeliveryPushProducer deliveryPushProducer(SqsClient sqs, ObjectMapper objMapper) {
        return new DeliveryPushProducer(sqs, properties.getTopics().getToDeliveryPush(), objMapper);
    }

    @Bean
    UserAttributesProducer userAttributesProducer(SqsClient sqs, ObjectMapper objMapper) {
        return new UserAttributesProducer(sqs, properties.getTopics().getToUserAttributes(), objMapper);
    }

    @Bean
    PaperChannelProducer paperChannelProducer(SqsClient sqs, ObjectMapper objMapper) {
        return new PaperChannelProducer(sqs, properties.getTopics().getToPaperChannel(), objMapper);
    }

    @Bean
    InternalProducer internalProducer(SqsClient sqs, ObjectMapper objMapper) {
        return new InternalProducer(sqs, properties.getTopics().getToInternal(), objMapper);
    }

    @Bean
    OcrProducer ocrProducer(SqsClient sqs, ObjectMapper objMapper) {
        return new OcrProducer(sqs, properties.getTopics().getOcrOutput(), objMapper);
    }

}
