package it.pagopa.pn.externalchannels.middleware.extchannelwebhook;

import it.pagopa.pn.externalchannels.config.PnExternalChannelsProperties;
import it.pagopa.pn.externalchannels.exception.ExternalChannelsMockException;
import it.pagopa.pn.externalchannels.generated.openapi.clients.extchannelwebhook.ApiClient;
import it.pagopa.pn.externalchannels.generated.openapi.clients.extchannelwebhook.api.DefaultApi;
import it.pagopa.pn.externalchannels.generated.openapi.clients.extchannelwebhook.model.OperationResultCodeResponse;
import it.pagopa.pn.externalchannels.generated.openapi.clients.extchannelwebhook.model.PaperProgressStatusEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Slf4j
@Component
@EnableRetry
public class ExtChannelWebhookClientImpl implements ExtChannelWebhookClient {

    private final DefaultApi defaultApi;
    private final PnExternalChannelsProperties cfg;

    public ExtChannelWebhookClientImpl(@Qualifier("withTracing") RestTemplate restTemplate, PnExternalChannelsProperties cfg) {
        ApiClient newApiClient = new ApiClient( restTemplate );
        newApiClient.setBasePath( cfg.getSafeStorageBaseUrl() );

        this.defaultApi = new DefaultApi( newApiClient );
        this.cfg = cfg;
    }

    @Override
    public OperationResultCodeResponse sendPaperProgressStatusRequest(PaperProgressStatusEvent event){
        try {
            log.debug("Start call sendPaperProgressStatusRequest - requestId={} statusCode={}", event.getRequestId(), event.getStatusCode());

            OperationResultCodeResponse operationResultCodeResponse = defaultApi.sendPaperProgressStatusRequest(
                    cfg.getExtchannelwebhookServiceid(), cfg.getExtchannelwebhookApiKey(), List.of(event) );

            log.debug("End call sendPaperProgressStatusRequest requestId={} res={}", event.getRequestId(), operationResultCodeResponse.getResultCode());

            return operationResultCodeResponse;
        } catch (RestClientException e) {
            throw new ExternalChannelsMockException("Exception invoking sendPaperProgressStatusRequest", e);
        }
    }
}
