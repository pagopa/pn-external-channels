package it.pagopa.pn.externalchannels.middleware.addressmanager;

import it.pagopa.pn.externalchannels.config.PnExternalChannelsProperties;
import it.pagopa.pn.externalchannels.generated.openapi.clients.pnaddressmanager.ApiClient;
import it.pagopa.pn.commons.pnclients.CommonBaseClient;
import org.springframework.stereotype.Component;

@Component
public class PnAddressManagerWebClient extends CommonBaseClient {

    private final PnExternalChannelsProperties cfg;

    public PnAddressManagerWebClient(PnExternalChannelsProperties cfg) {
        this.cfg = cfg;
    }

    public ApiClient init() {
        ApiClient apiClient = new ApiClient(super.initWebClient(ApiClient.buildWebClientBuilder()));
        apiClient.setBasePath(cfg.getAddressManagerBaseUrl());
        return apiClient;
    }
}
