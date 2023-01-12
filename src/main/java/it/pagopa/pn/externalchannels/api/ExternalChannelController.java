package it.pagopa.pn.externalchannels.api;

import it.pagopa.pn.externalchannels.model.PaperEngageRequest;
import it.pagopa.pn.externalchannels.service.ExternalChannelsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.Objects;

@RestController
@RequiredArgsConstructor
@Slf4j
public class ExternalChannelController implements ExternalChannelApi {

    private static final String APP_SOURCE_NAME = "x-pagopa-extch-cx-id";

    private final ExternalChannelsService externalChannelsService;


    @Override
    public Mono<ResponseEntity<Void>> sendPaperEngageRequest(String requestIdx, String xPagopaExtchCxId,
                                                             Mono<PaperEngageRequest> paperEngageRequest,
                                                             final ServerWebExchange exchange) {

//        String appSourceName = getAppSourceName(exchange);
        String appSourceName = "PAPER_CHANNEL";
        //per ora sono sicuro che chi chiama questa API è solo paper channel

        return paperEngageRequest
                .doOnNext(request -> log.info("Received request with requestBody: {}, headers: {}", request, exchange.getRequest().getHeaders()))
                .doOnNext(request -> externalChannelsService.sendPaperEngageRequest(request, appSourceName))
                .map(notificationRequest -> Mono.just(ResponseEntity.noContent().build()))
                .log(this.getClass().getName())
                .onErrorResume(Mono::error).then(Mono.just(ResponseEntity.noContent().build()));

    }

    private String getAppSourceName(ServerWebExchange exchange) {
        if(exchange.getRequest().getHeaders().containsKey(APP_SOURCE_NAME) &&
                !Objects.requireNonNull(exchange.getRequest().getHeaders().get(APP_SOURCE_NAME)).isEmpty()) {
            return Objects.requireNonNull(exchange.getRequest().getHeaders().get(APP_SOURCE_NAME)).get(0);
        }

        return null;
    }
}
