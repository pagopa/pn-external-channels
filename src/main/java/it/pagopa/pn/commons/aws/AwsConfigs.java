package it.pagopa.pn.commons.aws;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties("aws")
public class AwsConfigs {

    private String profileName;
    private String regionCode;
    private String endpointUrl;

    public void setProfileName(String profileName) {
        this.profileName = profileName;
    }

    public void setRegionCode(String regionCode) {
        this.regionCode = regionCode;
    }

    public String getProfileName() {
        return profileName;
    }

    public String getRegionCode() {
        return regionCode;
    }

    public String getEndpointUrl() {
        return endpointUrl;
    }

    public void setEndpointUrl(String endpointUrl) {
        this.endpointUrl = endpointUrl;
    }
}
