package it.pagopa.pn.externalchannels.middleware.extchannelwebhook;

import it.pagopa.pn.commons.utils.LogUtils;
import it.pagopa.pn.externalchannels.dto.NotificationProgress;
import it.pagopa.pn.externalchannels.generated.openapi.clients.extchannelwebhook.ApiClient;
import it.pagopa.pn.externalchannels.generated.openapi.clients.extchannelwebhook.api.DefaultApi;
import it.pagopa.pn.externalchannels.generated.openapi.clients.extchannelwebhook.model.*;
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
            log.info("sendPaperProgressStatusRequest - event={}", event);
            OperationResultCodeResponse operationResultCodeResponse = defaultApi.sendPaperProgressStatusRequest(
                    notificationProgress.getOutputServiceId(), notificationProgress.getOutputApiKey(), List.of(event) );

            log.debug("End call sendPaperProgressStatusRequest requestId={} res={}", event.getRequestId(), operationResultCodeResponse.getResultCode());

            return operationResultCodeResponse;
        } catch (Exception e) {
            log.error("Exception invoking sendPaperProgressStatusRequest", e);
            return null;
        }
    }



    @Override
    public PreLoadResponse presignedUploadRequest(NotificationProgress notificationProgress, PreLoadRequest preloadRequest){
        try {
            ApiClient newApiClient = new ApiClient( restTemplate );
            newApiClient.setBasePath( notificationProgress.getOutputEndpoint());

            DefaultApi defaultApi = new DefaultApi( newApiClient );

            log.info("Start call presignedUploadRequest - preloadIdx={} endoint={} serviceid={} apikey={}", preloadRequest.getPreloadIdx(),
                     notificationProgress.getOutputEndpoint(), notificationProgress.getOutputServiceId(), LogUtils.maskGeneric(notificationProgress.getOutputApiKey()));
            log.info("presignedUploadRequest - preloadRequest={}", preloadRequest);

            InlineObject preloadRequests = new InlineObject();
            preloadRequests.setPreloads(List.of(preloadRequest));

            InlineResponse200 response200 = defaultApi.presignedUploadRequest(notificationProgress.getOutputServiceId(), notificationProgress.getOutputApiKey(), preloadRequests);

            PreLoadResponse preLoadResponse = response200.getPreloads().get(0);

            log.debug("End call presignedUploadRequest preloadIdx={} res={}", preloadRequest.getPreloadIdx(), preLoadResponse.getKey());

            return preLoadResponse;
        } catch (Exception e) {
            log.error("Exception invoking presignedUploadRequest", e);
            return null;
        }
    }
}
