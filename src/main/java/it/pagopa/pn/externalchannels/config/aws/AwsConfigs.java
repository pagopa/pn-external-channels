package it.pagopa.pn.externalchannels.config.aws;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Getter
@Setter
@ToString
@Configuration
@ConfigurationProperties("aws")
public class AwsConfigs {

    private String profileName;
    private String regionCode;
    private String endpointUrl;

    private String accessKeyId;
    private String secretAccessKey;
}