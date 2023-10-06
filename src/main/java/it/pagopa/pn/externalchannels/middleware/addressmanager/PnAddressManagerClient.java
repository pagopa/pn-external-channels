package it.pagopa.pn.externalchannels.middleware.addressmanager;

import it.pagopa.pn.externalchannels.generated.openapi.clients.pnaddressmanager.model.CallbackRequestData;
import it.pagopa.pn.externalchannels.generated.openapi.clients.pnaddressmanager.model.CallbackResponseData;
import it.pagopa.pn.externalchannels.generated.openapi.clients.pnaddressmanager.model.PreLoadRequestData;
import it.pagopa.pn.externalchannels.generated.openapi.clients.pnaddressmanager.model.PreLoadResponseData;
import reactor.core.publisher.Mono;

public interface PnAddressManagerClient {

    public Mono<PreLoadResponseData> getPresignedURI(String cxId, String xApiKey, PreLoadRequestData preLoadRequest);

    public Mono<CallbackResponseData> performCallback(String cxId, String xApiKey, CallbackRequestData callbackRequestData);
}
