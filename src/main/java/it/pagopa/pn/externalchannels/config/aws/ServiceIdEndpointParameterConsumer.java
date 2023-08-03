package it.pagopa.pn.externalchannels.config.aws;

import it.pagopa.pn.externalchannels.config.PnExternalChannelsProperties;
import it.pagopa.pn.externalchannels.util.ParameterizedCachedSsmParameterConsumer;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.ssm.SsmClient;

import java.time.Duration;

@Component
public class ServiceIdEndpointParameterConsumer extends ParameterizedCachedSsmParameterConsumer {

    public ServiceIdEndpointParameterConsumer(SsmClient ssmClient, PnExternalChannelsProperties pnExternalChannelsProperties) {
        super(ssmClient, Duration.ofMinutes(pnExternalChannelsProperties.getCacheExpSsmMinutes()));
    }

}
