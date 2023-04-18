package it.pagopa.pn.externalchannels.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import it.pagopa.pn.externalchannels.sqs.DeliveryPushProducer;
import it.pagopa.pn.externalchannels.sqs.PaperChannelProducer;
import it.pagopa.pn.externalchannels.sqs.UserAttributesProducer;
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

}
