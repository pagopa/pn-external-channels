package it.pagopa.pn.externalchannels.config;


import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import it.pagopa.pn.commons.configs.PnCassandraAutoConfiguration;
import it.pagopa.pn.commons.configs.RuntimeModeHolder;
import it.pagopa.pn.commons.configs.aws.AwsConfigs;
import it.pagopa.pn.commons.configs.aws.AwsServicesClientsConfig;
import it.pagopa.pn.externalchannels.binding.PnExtChnProcessor;
import it.pagopa.pn.externalchannels.config.properties.CloudAwsProperties;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.data.cassandra.repository.config.EnableCassandraRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.aws.mcs.auth.SigV4AuthProvider;

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
@Import( {PnCassandraAutoConfiguration.class, RuntimeModeHolder.class, AwsConfigs.class })
public class Config {

    /*@Bean
    @ConditionalOnProperty(name = "file-transfer-service.implementation", havingValue = "aws")
    public AmazonS3 s3client(CloudAwsProperties props){
        AWSCredentials credentials = new BasicAWSCredentials(
                props.getAccessKey(),
                props.getSecretKey()
        );

        return AmazonS3ClientBuilder
                .standard()
                .withEndpointConfiguration(new AwsClientBuilder.EndpointConfiguration(
                        props.getEndpoint(),
                        props.getRegion()
                ))
                .withCredentials(new AWSStaticCredentialsProvider(credentials))
                .enablePathStyleAccess()
                .build();
    }*/

    @Bean
    public ModelMapper modelMapper(){
        return new ModelMapper();
    }

    @PostConstruct
    public void init() {
        log.debug("External Channels Application has been configured.");
    }

}
