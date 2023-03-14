package it.pagopa.pn.externalchannels.middleware.extchannelwebhook;

import it.pagopa.pn.commons.utils.LogUtils;
import it.pagopa.pn.externalchannels.dto.NotificationProgress;
import it.pagopa.pn.externalchannels.generated.openapi.clients.extchannelwebhook.ApiClient;
import it.pagopa.pn.externalchannels.generated.openapi.clients.extchannelwebhook.api.DefaultApi;
import it.pagopa.pn.externalchannels.generated.openapi.clients.extchannelwebhook.model.OperationResultCodeResponse;
import it.pagopa.pn.externalchannels.generated.openapi.clients.extchannelwebhook.model.PaperProgressStatusEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Slf4j
@Component
@EnableRetry
public class ExtChannelWebhookClientImpl implements ExtChannelWebhookClient {

    private final RestTemplate restTemplate;
    public ExtChannelWebhookClientImpl(@Qualifier("withTracing") RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @Override
    public OperationResultCodeResponse sendPaperProgressStatusRequest(NotificationProgress notificationProgress, PaperProgressStatusEvent event){
        try {
            ApiClient newApiClient = new ApiClient( restTemplate );
            newApiClient.setBasePath( notificationProgress.getOutputEndpoint());

            DefaultApi defaultApi = new DefaultApi( newApiClient );

            log.info("Start call sendPaperProgressStatusRequest - requestId={} statusCode={} endoint={} serviceid={} apikey={}", event.getRequestId(), event.getStatusCode(), notificationProgress.getOutputEndpoint(), notificationProgress.getOutputServiceId(), LogUtils.maskGeneric(notificationProgress.getOutputApiKey()));

            OperationResultCodeResponse operationResultCodeResponse = defaultApi.sendPaperProgressStatusRequest(
                    notificationProgress.getOutputServiceId(), notificationProgress.getOutputApiKey(), List.of(event) );

            log.debug("End call sendPaperProgressStatusRequest requestId={} res={}", event.getRequestId(), operationResultCodeResponse.getResultCode());

            return operationResultCodeResponse;
        } catch (Exception e) {
            log.error("Exception invoking sendPaperProgressStatusRequest", e);
            return null;
        }
    }
}
