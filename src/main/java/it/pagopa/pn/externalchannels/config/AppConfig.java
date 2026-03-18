package it.pagopa.pn.externalchannels.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import it.pagopa.pn.externalchannels.sqs.producer.*;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.eventbridge.EventBridgeClient;
import software.amazon.awssdk.services.eventbridge.EventBridgeClientBuilder;
import software.amazon.awssdk.services.sqs.SqsClient;

@Configuration
@EnableScheduling
@EnableConfigurationProperties
@RequiredArgsConstructor
public class AppConfig {

    private final PnExternalChannelsProperties properties;

    @Bean
    EventBridgeClient eventBridgeSyncClient(@Value("${aws.region-code}") String region, @Value("${aws.endpoint-url}") String endpointUrl) {
        EventBridgeClientBuilder builder = EventBridgeClient.builder()
                .credentialsProvider(DefaultCredentialsProvider.create());

        if (region != null) {
            builder.region(Region.of(region));
        }

        if (endpointUrl != null) {
            builder.endpointOverride(java.net.URI.create(endpointUrl));
        }

        return builder.build();
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
        return new OcrProducer(sqs, properties.getTopics().getOcrOutputs(), objMapper);
    }

}
