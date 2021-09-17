package it.pagopa.pn.externalchannels.config;

import it.pagopa.pn.externalchannels.binding.PnExtChnProcessor;
import it.pagopa.pn.externalchannels.jod.wsclient.PnExtChnJodClient;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.cassandra.repository.config.EnableCassandraRepositories;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;
import org.springframework.scheduling.annotation.EnableScheduling;

import javax.annotation.PostConstruct;

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
    public ModelMapper modelMapper() {
        return new ModelMapper();
    }

    @PostConstruct
    public void init() {
        log.debug("External Channels Application has been configured.");
    }

}
