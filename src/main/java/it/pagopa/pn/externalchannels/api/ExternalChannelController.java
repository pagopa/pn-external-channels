package it.pagopa.pn.externalchannels.api;

import it.pagopa.pn.externalchannels.model.PaperEngageRequest;
import it.pagopa.pn.externalchannels.service.ExternalChannelsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@RestController
@RequiredArgsConstructor
public class ExternalChannelController implements ExternalChannelApi {

    private static final String APP_SOURCE_NAME = "x-pagopa-extch-cx-id";

    private final ExternalChannelsService externalChannelsService;


    @Override
    public Mono<ResponseEntity<Void>> sendPaperEngageRequest(String requestIdx, String xPagopaExtchCxId,
                                                             Mono<PaperEngageRequest> paperEngageRequest,
                                                             final ServerWebExchange exchange) {


        String appSourceName = exchange.getRequest().getHeaders().get(APP_SOURCE_NAME).get(0);

        return paperEngageRequest
                .doOnNext(request -> externalChannelsService.sendPaperEngageRequest(request, appSourceName))
                .map(notificationRequest -> Mono.just(ResponseEntity.noContent().build()))
                .log(this.getClass().getName())
                .onErrorResume(Mono::error).then(Mono.just(ResponseEntity.noContent().build()));

    }
}
