package it.pagopa.pn.externalchannels.config.aws;


import it.pagopa.pn.commons.configs.RuntimeMode;
import it.pagopa.pn.commons.configs.aws.AwsConfigs;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;


@Configuration
@Slf4j
public class AwsServicesClientsConfig extends it.pagopa.pn.commons.configs.aws.AwsServicesClientsConfig {

    private AwsConfigs props;

    public AwsServicesClientsConfig(AwsConfigs props) {
        super(props, RuntimeMode.PROD);
    }

}
