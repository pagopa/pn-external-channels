package it.pagopa.pn.externalchannels.config.aws;

import it.pagopa.pn.commons.abstractions.impl.AbstractCachedSsmParameterConsumer;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.ssm.SsmClient;

@Component
public class ServiceIdEndpointParameterConsumer extends AbstractCachedSsmParameterConsumer {

    public ServiceIdEndpointParameterConsumer(SsmClient ssmClient) {
        super(ssmClient);
    }

}
