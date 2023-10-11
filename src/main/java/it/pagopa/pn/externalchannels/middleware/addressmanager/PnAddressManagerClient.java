package it.pagopa.pn.externalchannels.middleware.addressmanager;

import it.pagopa.pn.externalchannels.generated.openapi.clients.pnaddressmanager.model.*;


public interface PnAddressManagerClient {

    PreLoadResponseData getPresignedURI(String cxId, String xApiKey, PreLoadRequestData preLoadRequest);

    FileDownloadResponse getFile(String cxId, String xApiKey, String fileKey);

    OperationResultCodeResponse performCallback(String cxId, String xApiKey, NormalizerCallbackRequest callbackRequestData);
}
