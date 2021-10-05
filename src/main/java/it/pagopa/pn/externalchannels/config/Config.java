package it.pagopa.pn.externalchannels.config;


import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.datatype.jsr310.JSR310Module;
import com.fasterxml.jackson.datatype.jsr310.ser.InstantSerializer;
import it.pagopa.pn.commons.configs.PnCassandraAutoConfiguration;
import it.pagopa.pn.commons.configs.RuntimeModeHolder;
import it.pagopa.pn.commons.configs.aws.AwsConfigs;
import it.pagopa.pn.externalchannels.binding.PnExtChnProcessor;
import it.pagopa.pn.externalchannels.config.properties.CloudAwsProperties;
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
import org.springframework.scheduling.annotation.EnableScheduling;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.aws.mcs.auth.SigV4AuthProvider;

import javax.annotation.PostConstruct;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.format.DateTimeFormatterBuilder;
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
    public AmazonS3 s3client(CloudAwsProperties props){
        String regionCode = props.getRegion();

        AmazonS3ClientBuilder builder = AmazonS3ClientBuilder.standard();

        String endpointUrl = props.getEndpoint();
        if( StringUtils.isNotBlank( endpointUrl) ) {
            builder = builder.withEndpointConfiguration(new AwsClientBuilder.EndpointConfiguration(
                        endpointUrl, regionCode
                    ));
        }
        else {
            builder = builder.withRegion( regionCode );
        }



        String profileName = props.getProfileName();
        if( StringUtils.isNotBlank( profileName )) {
            ProfileCredentialsProvider profCred = new ProfileCredentialsProvider(profileName);
            builder = builder.withCredentials( profCred );
        }

        return builder.enablePathStyleAccess()
                .build();
    }

    @Bean
    public ModelMapper modelMapper(){
        return new ModelMapper();
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

    @PostConstruct
    public void init() {
        log.debug("External Channels Application has been configured.");
    }

}
