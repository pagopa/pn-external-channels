package it.pagopa.pn.externalchannels.middleware.addressmanager;

import it.pagopa.pn.commons.pnclients.CommonBaseClient;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

@lombok.CustomLog
public abstract class CommonWebClient extends CommonBaseClient {

    protected final WebClient initWebClient(HttpClient httpClient, String baseUrl) {

        ExchangeStrategies strategies = ExchangeStrategies.builder().codecs(configurer -> {
            configurer.registerDefaults(true);
            configurer.customCodecs().register(new CustomFormMessageWriter());
        }).build();

        return super.enrichBuilder(WebClient.builder()
                        .baseUrl(baseUrl)
                        .exchangeStrategies(strategies)
                        .codecs(c -> c.defaultCodecs().enableLoggingRequestDetails(true))
                        .clientConnector(new ReactorClientHttpConnector(httpClient)))
                .build();
    }
}
