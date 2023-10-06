package it.pagopa.pn.externalchannels.middleware.addressmanager;

import it.pagopa.pn.commons.log.PnLogger;
import it.pagopa.pn.externalchannels.config.PnExternalChannelsProperties;
import it.pagopa.pn.externalchannels.generated.openapi.clients.pnaddressmanager.ApiClient;
import it.pagopa.pn.externalchannels.generated.openapi.clients.pnaddressmanager.api.NormalizzatoreApi;
import it.pagopa.pn.externalchannels.generated.openapi.clients.pnaddressmanager.model.CallbackRequestData;
import it.pagopa.pn.externalchannels.generated.openapi.clients.pnaddressmanager.model.CallbackResponseData;
import it.pagopa.pn.externalchannels.generated.openapi.clients.pnaddressmanager.model.PreLoadRequestData;
import it.pagopa.pn.externalchannels.generated.openapi.clients.pnaddressmanager.model.PreLoadResponseData;
import lombok.CustomLog;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import reactor.core.publisher.Mono;

@CustomLog
@Component
public class PnAddressManagerClientImpl implements  PnAddressManagerClient{

    private final NormalizzatoreApi normalizzatoreApi;
    public PnAddressManagerClientImpl(@Qualifier("withTracing") RestTemplate restTemplate,
                                      PnExternalChannelsProperties properties) {
        ApiClient newApiClient = new ApiClient( restTemplate );
        newApiClient.setBasePath(properties.getAddressManagerBaseUrl());
        normalizzatoreApi = new NormalizzatoreApi(newApiClient);
    }

    @Override
    public Mono<PreLoadResponseData> getPresignedURI(String cxId, String xApiKey, PreLoadRequestData preLoadRequest) {
        log.logInvokingExternalService(PnLogger.EXTERNAL_SERVICES.PN_EXTERNAL_CHANNELS, "getPresignedURI");
        return Mono.just(normalizzatoreApi.presignedUploadRequest(cxId, xApiKey, preLoadRequest));
    }

    @Override
    public Mono<CallbackResponseData> performCallback(String cxId, String xApiKey, CallbackRequestData callbackRequestData) {
        log.logInvokingExternalService(PnLogger.EXTERNAL_SERVICES.PN_EXTERNAL_CHANNELS, "performCallback");
        return Mono.just(normalizzatoreApi.callbackNormalizedAddress(cxId, xApiKey, callbackRequestData));
    }
}
