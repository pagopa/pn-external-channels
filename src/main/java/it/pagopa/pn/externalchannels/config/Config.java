package it.pagopa.pn.externalchannels.config;


import it.pagopa.pn.externalchannels.binding.PnExtChnProcessor;
import it.pagopa.pn.externalchannels.jod.wsclient.PnExtChnJodClient;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.common.TopicPartition;
import org.modelmapper.ModelMapper;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.binder.kafka.BinderHeaderMapper;
import org.springframework.cloud.stream.binder.kafka.KafkaBindingRebalanceListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.cassandra.repository.config.EnableCassandraRepositories;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;
import org.springframework.kafka.support.DefaultKafkaHeaderMapper;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;
import org.springframework.scheduling.annotation.EnableScheduling;

import javax.annotation.PostConstruct;
import java.util.Collection;

import static it.pagopa.pn.api.dto.events.StandardEventHeader.*;

@Configuration
@ConfigurationProperties
@EnableScheduling
@EnableCassandraRepositories(basePackages = "it.pagopa.pn.externalchannels.repositories.cassandra")
// @EnableMongoRepositories(basePackages = "it.pagopa.pn.externalchannels.repositories.mongo")
@EnableConfigurationProperties
@ComponentScan(basePackages = "it.pagopa.pn.externalchannels")
@EnableBinding({PnExtChnProcessor.class})
@Slf4j
public class Config {

// TODO: STRADA DA PROVARE PER L'AUTENTICAZIONE SU JOD
//    @Bean
//    public Wss4jSecurityInterceptor wss4jSecurityInterceptor() {
//        Wss4jSecurityInterceptor interceptor = new Wss4jSecurityInterceptor();
//        interceptor.setSecurementActions("UsernameToken");
//        interceptor.setSecurementPasswordType("PasswordText");
//        return interceptor;
//    }

    @Bean
    @ConditionalOnProperty(name = "file-transfer-service.implementation", havingValue = "jod")
    public Jaxb2Marshaller jodFtpMarshaller() {
        Jaxb2Marshaller marshaller = new Jaxb2Marshaller();
        marshaller.setContextPath("it.pagopa.pn.externalchannels.jod.wsclient");
        return marshaller;
    }

    @Bean
    @ConditionalOnProperty(name = "file-transfer-service.implementation", havingValue = "jod")
    public PnExtChnJodClient pnExtChnJodClient(Jaxb2Marshaller jodFtpMarshaller) {
        PnExtChnJodClient client = new PnExtChnJodClient();
        client.setDefaultUri("http://localhost:8080/ws");
        client.setMarshaller(jodFtpMarshaller);
        client.setUnmarshaller(jodFtpMarshaller);
        return client;
    }

    @Bean
    public ModelMapper modelMapper(){
        return new ModelMapper();
    }

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
        defaultKafkaHeaderMapper.setMapAllStringsOut(false);
        defaultKafkaHeaderMapper.addRawMappedHeader(PN_EVENT_HEADER_PUBLISHER, true);
        defaultKafkaHeaderMapper.addRawMappedHeader(PN_EVENT_HEADER_EVENT_ID, true);
        defaultKafkaHeaderMapper.addRawMappedHeader(PN_EVENT_HEADER_EVENT_TYPE, true);
        defaultKafkaHeaderMapper.addRawMappedHeader(PN_EVENT_HEADER_IUN, true);
        defaultKafkaHeaderMapper.addRawMappedHeader(PN_EVENT_HEADER_CREATED_AT, true);
        return defaultKafkaHeaderMapper;
    }

}
