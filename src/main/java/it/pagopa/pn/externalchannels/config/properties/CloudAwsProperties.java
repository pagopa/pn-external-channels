package it.pagopa.pn.externalchannels.config.properties;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Data
@Component
public class CloudAwsProperties {

    @Value("${cloud.aws.credentials.profile-name:}")
    private String profileName;
    @Value("${cloud.aws.sqs.endpoint:}")
    private String endpoint;
    @Value("${cloud.aws.region.static}")
    private String region;

}
