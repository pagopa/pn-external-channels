package it.pagopa.pn.externalchannels.middleware.addressmanager;

import it.pagopa.pn.commons.log.PnLogger;
import it.pagopa.pn.externalchannels.generated.openapi.clients.pnaddressmanager.api.NormalizzatoreApi;
import it.pagopa.pn.externalchannels.generated.openapi.clients.pnaddressmanager.model.*;
import lombok.CustomLog;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@CustomLog
@Component
public class PnAddressManagerClientImpl implements  PnAddressManagerClient{

    private final NormalizzatoreApi normalizzatoreApi;

    public PnAddressManagerClientImpl(PnAddressManagerWebClient pnAddressManagerWebClient) {
       this.normalizzatoreApi = new NormalizzatoreApi(pnAddressManagerWebClient.init());
    }

    @Override
    public Mono<PreLoadResponseData> getPresignedURI(String cxId, String xApiKey, PreLoadRequestData preLoadRequest) {
        log.logInvokingExternalService(PnLogger.EXTERNAL_SERVICES.PN_EXTERNAL_CHANNELS, "getPresignedURI");
        return normalizzatoreApi.presignedUploadRequest(cxId, xApiKey, preLoadRequest);
    }

    @Override
    public Mono<OperationResultCodeResponse> performCallback(String cxId, String xApiKey, NormalizerCallbackRequest callbackRequestData) {
        log.logInvokingExternalService(PnLogger.EXTERNAL_SERVICES.PN_EXTERNAL_CHANNELS, "performCallback");
        return normalizzatoreApi.normalizerCallback(cxId, xApiKey, callbackRequestData);
    }
}
