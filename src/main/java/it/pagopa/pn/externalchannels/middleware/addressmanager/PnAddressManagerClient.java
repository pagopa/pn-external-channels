package it.pagopa.pn.externalchannels.middleware.addressmanager;

import it.pagopa.pn.externalchannels.generated.openapi.clients.pnaddressmanager.model.*;
import reactor.core.publisher.Mono;


public interface PnAddressManagerClient {

    PreLoadResponseData getPresignedURI(String cxId, String xApiKey, PreLoadRequestData preLoadRequest);

    OperationResultCodeResponse performCallback(String cxId, String xApiKey, NormalizerCallbackRequest callbackRequestData);
}
