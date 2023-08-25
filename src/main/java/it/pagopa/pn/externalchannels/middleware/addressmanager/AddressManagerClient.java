package it.pagopa.pn.externalchannels.middleware.addressmanager;


import it.pagopa.pn.commons.log.PnLogger;
import it.pagopa.pn.externalchannels.exception.ExternalChannelsMockException;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

@Component
@lombok.CustomLog
public class AddressManagerClient {

    private final WebClient webClient;

    protected AddressManagerClient(AddressManagerWebClient addressManagerWebClient) {
        this.webClient = addressManagerWebClient.init();
    }

    public Mono<String> getPresignedURI(String fileKey){
        log.logInvokingExternalService(PnLogger.EXTERNAL_SERVICES.PN_EXTERNAL_CHANNELS, "getPresignedURI");
        return webClient.post()
                .uri("/getPresignedURI")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(fileKey)
                .retrieve()
                .bodyToMono(String.class)
                .doOnError(this::handleDeduplicaOnlineError);
    }

    public Mono<String> performCallback(String keyInput){
        log.logInvokingExternalService(PnLogger.EXTERNAL_SERVICES.PN_EXTERNAL_CHANNELS, "getPresignedURI");
        return webClient.post()
                .uri("/performCallback")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(keyInput)
                .retrieve()
                .bodyToMono(String.class)
                .doOnError(this::handleDeduplicaOnlineError);
    }


    private void handleDeduplicaOnlineError(Throwable throwable){
        if (throwable instanceof WebClientResponseException ex) {
            throw new ExternalChannelsMockException(ex.getMessage(), throwable);
        }
    }

}
