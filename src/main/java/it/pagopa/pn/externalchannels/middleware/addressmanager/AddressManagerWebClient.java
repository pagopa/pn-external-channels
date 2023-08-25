package it.pagopa.pn.externalchannels.middleware.addressmanager;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;
import reactor.netty.resources.ConnectionProvider;

import java.time.Duration;

@Component
@lombok.CustomLog
public class AddressManagerWebClient extends CommonWebClient {

    private final Integer tcpMaxPoolSize;
    private final Integer tcpMaxQueuedConnections;
    private final Integer tcpPendingAcquireTimeout;
    private final Integer tcpPoolIdleTimeout;
    private final String basePath;

    public AddressManagerWebClient(@Value("${pn.external.channels.webclient.address-manager.tcp-max-poolsize}") Integer tcpMaxPoolSize,
                           @Value("${pn.external.channels.webclient.address-manager.tcp-max-queued-connections}") Integer tcpMaxQueuedConnections,
                           @Value("${pn.external.channels.webclient.address-manager.tcp-pending-acquired-timeout}") Integer tcpPendingAcquireTimeout,
                           @Value("${pn.external.channels.webclient.address-manager.tcp-pool-idle-timeout}") Integer tcpPoolIdleTimeout,
                           @Value("${pn.external.channels.webclient.address-manager.base-path}") String basePath) {
        this.tcpMaxPoolSize = tcpMaxPoolSize;
        this.tcpMaxQueuedConnections = tcpMaxQueuedConnections;
        this.tcpPendingAcquireTimeout = tcpPendingAcquireTimeout;
        this.tcpPoolIdleTimeout = tcpPoolIdleTimeout;
        this.basePath = basePath;
    }

    public WebClient init() {
        ConnectionProvider provider = ConnectionProvider.builder("fixed")
                .maxConnections(tcpMaxPoolSize)
                .pendingAcquireMaxCount(tcpMaxQueuedConnections)
                .pendingAcquireTimeout(Duration.ofMillis(tcpPendingAcquireTimeout))
                .maxIdleTime(Duration.ofMillis(tcpPoolIdleTimeout)).build();

        HttpClient httpClient = HttpClient.create(provider);

        return super.initWebClient(httpClient, basePath);
    }
}
