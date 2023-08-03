package it.pagopa.pn.externalchannels.config.aws;

import it.pagopa.pn.externalchannels.config.PnExternalChannelsProperties;
import it.pagopa.pn.externalchannels.util.ParameterizedCachedSsmParameterConsumer;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.ssm.SsmClient;

import java.time.Duration;

@Component
public class EventCodeSequenceParameterConsumer extends ParameterizedCachedSsmParameterConsumer {

    public EventCodeSequenceParameterConsumer(SsmClient ssmClient, PnExternalChannelsProperties pnExternalChannelsProperties) {
        super(ssmClient, Duration.ofMinutes(pnExternalChannelsProperties.getCacheExpSsmMinutes()));
    }

}
