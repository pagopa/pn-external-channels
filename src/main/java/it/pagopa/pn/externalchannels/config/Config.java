package it.pagopa.pn.externalchannels.config;


import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.sqs.AmazonSQSAsync;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.datatype.jsr310.JSR310Module;
import com.fasterxml.jackson.datatype.jsr310.ser.InstantSerializer;
import io.awspring.cloud.core.env.ResourceIdResolver;
import io.awspring.cloud.messaging.core.QueueMessagingTemplate;
import it.pagopa.pn.api.dto.events.PnExtChnPaperEventPayload;
import it.pagopa.pn.api.dto.events.PnExtChnPecEventPayload;
import it.pagopa.pn.commons.configs.PnCassandraAutoConfiguration;
import it.pagopa.pn.commons.configs.RuntimeModeHolder;
import it.pagopa.pn.commons.configs.aws.AwsConfigs;
import it.pagopa.pn.externalchannels.binding.PnExtChnProcessor;
import it.pagopa.pn.externalchannels.config.properties.EmailProperties;
import it.pagopa.pn.externalchannels.config.properties.S3Properties;
import it.pagopa.pn.externalchannels.entities.queuedmessage.QueuedMessage;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.modelmapper.ModelMapper;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.data.cassandra.repository.config.EnableCassandraRepositories;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.scheduling.annotation.EnableScheduling;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.aws.mcs.auth.SigV4AuthProvider;

import javax.annotation.PostConstruct;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.format.DateTimeFormatterBuilder;
import java.util.Properties;
import java.util.TimeZone;

import static java.time.ZoneOffset.UTC;

@Configuration
@ConfigurationProperties
@EnableScheduling
@EnableCassandraRepositories(basePackages = "it.pagopa.pn.externalchannels.repositories.cassandra")
// @EnableMongoRepositories(basePackages = "it.pagopa.pn.externalchannels.repositories.mongo")
@EnableConfigurationProperties
@ComponentScan(basePackages = "it.pagopa.pn.externalchannels")
@EnableBinding({PnExtChnProcessor.class})
@Slf4j
@Import( {PnCassandraAutoConfiguration.class, RuntimeModeHolder.class, AwsConfigs.class })
public class Config {

    @Bean
    @ConditionalOnProperty( name = "aws.use-aws-keyspace", havingValue = "true")
    public SigV4AuthProvider awsKeyspaceTokenProvider( AwsConfigs props) {

        DefaultCredentialsProvider.Builder credentialsBuilder = DefaultCredentialsProvider.builder();

        String profileName = props.getProfileName();
        if( StringUtils.isNotBlank( profileName ) ) {
            credentialsBuilder.profileName( profileName );
        }

        String regionCode = props.getRegionCode();
        return new SigV4AuthProvider( credentialsBuilder.build(), regionCode );
    }

    @Bean
    @ConditionalOnProperty(name = "file-transfer-service.implementation", havingValue = "aws")
    public AmazonS3 s3client(S3Properties props){

        AmazonS3ClientBuilder builder = AmazonS3ClientBuilder
                .standard();

        if(StringUtils.isNotBlank(props.getProfile())){
            ProfileCredentialsProvider profCred = new ProfileCredentialsProvider(props.getProfile());
            builder.withCredentials(profCred);
        }

        if(StringUtils.isNotBlank(props.getEndpoint())) {
            builder.withEndpointConfiguration(new AwsClientBuilder.EndpointConfiguration(
                    props.getEndpoint(),
                    props.getRegion()
            ));
        }
        else if(StringUtils.isNotEmpty(props.getRegion())) {
            builder.withRegion(props.getRegion());
        }


        builder.enablePathStyleAccess();

        return builder.build();
    }

    @Bean
    public ModelMapper modelMapper(){
        ModelMapper modelMapper = new ModelMapper();
        modelMapper.createTypeMap(PnExtChnPaperEventPayload.class, QueuedMessage.class)
                .addMapping(s -> s.getDestinationAddress().getAddress(), QueuedMessage::setAddress)
                .addMapping(s -> s.getDestinationAddress().getAddressDetails(), QueuedMessage::setAddressDetails)
                .addMapping(s -> s.getDestinationAddress().getAt(), QueuedMessage::setAt)
                .addMapping(s -> s.getDestinationAddress().getMunicipality(), QueuedMessage::setMunicipality)
                .addMapping(s -> s.getDestinationAddress().getProvince(), QueuedMessage::setProvince)
                .addMapping(s -> s.getDestinationAddress().getZip(), QueuedMessage::setZip);
        return modelMapper;
    }

    @Bean
    public JavaMailSenderImpl javaMailSender(EmailProperties emailProperties) {
        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
        mailSender.setHost(emailProperties.getHost());
        mailSender.setPort(emailProperties.getPort());

        mailSender.setUsername(emailProperties.getUsername());
        mailSender.setPassword(emailProperties.getPassword());

        Properties props = mailSender.getJavaMailProperties();
        props.put("mail.transport.protocol", emailProperties.getProtocol());
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.debug", "true");

        return mailSender;
    }

    @Bean
    public ObjectMapper objectMapper(Jackson2ObjectMapperBuilder jackson2ObjectMapperBuilder){
        ObjectMapper objectMapper = jackson2ObjectMapperBuilder.createXmlMapper(false).build();
        JSR310Module javaTimeModule = new JSR310Module();
        javaTimeModule.addSerializer(Instant.class, new MyInstantSerializer());
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
        objectMapper.registerModule(javaTimeModule);
        objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        objectMapper.configure(SerializationFeature.WRITE_DATE_TIMESTAMPS_AS_NANOSECONDS, false);
        objectMapper.setDateFormat(df);
        objectMapper.setTimeZone(TimeZone.getTimeZone(UTC));
        return objectMapper;
    }
    private static class MyInstantSerializer extends InstantSerializer {
        public MyInstantSerializer() {
            super(InstantSerializer.INSTANCE, false, false,
                    new DateTimeFormatterBuilder().appendInstant(3).toFormatter());
        }
        @Override
        public JsonSerializer<?> createContextual(SerializerProvider prov, BeanProperty property) {
            return this;
        }
    }

    @Bean
    public QueueMessagingTemplate statusQueueMessagingTemplate(AmazonSQSAsync sqsClient, ObjectMapper objectMapper){
        MappingJackson2MessageConverter jacksonMessageConverter =
                new MappingJackson2MessageConverter();
        jacksonMessageConverter.setSerializedPayloadClass(String.class);
        jacksonMessageConverter.setObjectMapper(objectMapper);
        jacksonMessageConverter.setStrictContentTypeMatch(false);

        return new QueueMessagingTemplate(sqsClient, (ResourceIdResolver) null, jacksonMessageConverter);
    }

    @PostConstruct
    public void init() {
        log.debug("External Channels Application has been configured.");
    }

}
