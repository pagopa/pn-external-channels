package it.pagopa.pn.externalchannels.config;

import java.util.Collection;
import javax.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.common.TopicPartition;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.binder.kafka.KafkaBindingRebalanceListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.support.DefaultKafkaHeaderMapper;
import it.pagopa.pn.extchannels.binding.PNExtChnInboundSink;

@Configuration
@ConfigurationProperties
@EnableConfigurationProperties
@ComponentScan(basePackages = "it.pagopa.pn.externalchannels")
@EnableBinding({PNExtChnInboundSink.class})
@Slf4j
public class Config {

    @Bean
    KafkaBindingRebalanceListener kafkaBindingRebalanceListener() {
        return new KafkaBindingRebalanceListener() {
            @Override
            public void onPartitionsRevokedBeforeCommit(String bindingName, Consumer<?, ?> consumer, Collection<TopicPartition> partitions) {
                KafkaBindingRebalanceListener.super.onPartitionsRevokedBeforeCommit(bindingName, consumer, partitions); //To change body of generated methods, choose Tools | Templates.
            }

            @Override
            public void onPartitionsRevokedAfterCommit(String bindingName, Consumer<?, ?> consumer, Collection<TopicPartition> partitions) {
                KafkaBindingRebalanceListener.super.onPartitionsRevokedAfterCommit(bindingName, consumer, partitions); //To change body of generated methods, choose Tools | Templates.
            }

            @Override
            public void onPartitionsAssigned(String bindingName, Consumer<?, ?> consumer, Collection<TopicPartition> partitions, boolean initial) {
                log.info("partition assigned = {}", partitions.toString());
                KafkaBindingRebalanceListener.super.onPartitionsAssigned(bindingName, consumer, partitions, initial); //To change body of generated methods, choose Tools | Templates.
            }

        };
    }

    @PostConstruct
    public void init() {
        log.debug("External Channels Application has been configured.");
    }

    @Bean
    public DefaultKafkaHeaderMapper defaultKafkaHeaderMapper() {
        DefaultKafkaHeaderMapper defaultKafkaHeaderMapper
                = new DefaultKafkaHeaderMapper();
        defaultKafkaHeaderMapper.setEncodeStrings(true);
        return defaultKafkaHeaderMapper;
    }

}
