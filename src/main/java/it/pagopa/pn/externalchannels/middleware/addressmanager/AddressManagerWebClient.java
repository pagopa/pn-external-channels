package it.pagopa.pn.externalchannels.middleware.addressmanager;

import it.pagopa.pn.externalchannels.config.PnExternalChannelsProperties;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;
import reactor.netty.resources.ConnectionProvider;

import java.time.Duration;

@Component
@lombok.CustomLog
public class AddressManagerWebClient extends CommonWebClient {

    private final PnExternalChannelsProperties properties;

    public AddressManagerWebClient(PnExternalChannelsProperties properties) {
        this.properties = properties;
    }

    public WebClient init() {
        ConnectionProvider provider = ConnectionProvider.builder("fixed")
                .maxConnections(properties.getAddressManager().getTcpMaxPoolsize())
                .pendingAcquireMaxCount(properties.getAddressManager().getTcpMaxQueuedConnections())
                .pendingAcquireTimeout(Duration.ofMillis(properties.getAddressManager().getTcpPendingAcquiredTimeout()))
                .maxIdleTime(Duration.ofMillis(properties.getAddressManager().getTcpPoolIdleTimeout()))
                .build();

        HttpClient httpClient = HttpClient.create(provider);

        return super.initWebClient(httpClient, properties.getAddressManager().getAddressManagerBaseUrl());
    }
}
