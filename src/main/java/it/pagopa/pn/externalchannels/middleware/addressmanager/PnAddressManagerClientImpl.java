package it.pagopa.pn.externalchannels.middleware.addressmanager;

import it.pagopa.pn.commons.log.PnLogger;
import it.pagopa.pn.externalchannels.config.PnExternalChannelsProperties;
import it.pagopa.pn.externalchannels.generated.openapi.clients.pnaddressmanager.ApiClient;
import it.pagopa.pn.externalchannels.generated.openapi.clients.pnaddressmanager.api.NormalizzatoreApi;
import it.pagopa.pn.externalchannels.generated.openapi.clients.pnaddressmanager.model.*;
import lombok.CustomLog;
import org.checkerframework.checker.units.qual.N;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@CustomLog
@Component
public class PnAddressManagerClientImpl implements  PnAddressManagerClient{

    private final NormalizzatoreApi normalizzatoreApi;

    public PnAddressManagerClientImpl(@Qualifier("withTracing") RestTemplate restTemplate,
                                      PnExternalChannelsProperties properties) {
        ApiClient newApiClient = new ApiClient(restTemplate);
        newApiClient.setBasePath(properties.getAddressManagerBaseUrl());
        this.normalizzatoreApi = new NormalizzatoreApi(newApiClient);

    }

    @Override
    public PreLoadResponseData getPresignedURI(String cxId, String xApiKey, PreLoadRequestData preLoadRequest) {
        log.logInvokingExternalService(PnLogger.EXTERNAL_SERVICES.PN_EXTERNAL_CHANNELS, "getPresignedURI");
        return normalizzatoreApi.presignedUploadRequest(cxId, xApiKey, preLoadRequest);
    }

    @Override
    public OperationResultCodeResponse performCallback(String cxId, String xApiKey, NormalizerCallbackRequest callbackRequestData) {
        log.logInvokingExternalService(PnLogger.EXTERNAL_SERVICES.PN_EXTERNAL_CHANNELS, "performCallback");
        return normalizzatoreApi.normalizerCallback(cxId, xApiKey, callbackRequestData);
    }
}
