package it.pagopa.pn.externalchannels.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import it.pagopa.pn.externalchannels.sqs.DeliveryPushEmailProducer;
import it.pagopa.pn.externalchannels.sqs.DeliveryPushPaperProducer;
import it.pagopa.pn.externalchannels.sqs.DeliveryPushPecProducer;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.client.RestTemplate;
import software.amazon.awssdk.services.sqs.SqsClient;

@Configuration
@EnableScheduling
@EnableConfigurationProperties
@RequiredArgsConstructor
public class AppConfig {

    private final PnExternalChannelsProperties properties;

    @Bean
    DeliveryPushPecProducer deliveryPushPecProducer(SqsClient sqs, ObjectMapper objMapper) {
        return new DeliveryPushPecProducer(sqs, properties.getTopics().getToDeliveryPushPec(), objMapper);
    }

    @Bean
    DeliveryPushEmailProducer deliveryPushEmailProducer(SqsClient sqs, ObjectMapper objMapper) {
        return new DeliveryPushEmailProducer(sqs, properties.getTopics().getToDeliveryPushEmail(), objMapper);
    }

    @Bean
    DeliveryPushPaperProducer deliveryPushPaperProducer(SqsClient sqs, ObjectMapper objMapper) {
        return new DeliveryPushPaperProducer(sqs, properties.getTopics().getToDeliveryPushPaper(), objMapper);
    }

    @Bean
    RestTemplate restTemplate() {
        return new RestTemplate();
    }
}
