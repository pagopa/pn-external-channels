package it.pagopa.pn.externalchannels.api;

import it.pagopa.pn.externalchannels.dto.NotificationProgress;
import it.pagopa.pn.externalchannels.model.DigitalCourtesyMailRequest;
import it.pagopa.pn.externalchannels.model.DigitalCourtesySmsRequest;
import it.pagopa.pn.externalchannels.model.DigitalNotificationRequest;
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
public class ExternalChannelsController implements ExternalChannelsApi {

    private static final String APP_SOURCE_NAME = "x-pagopa-extch-cx-id";

    private final ExternalChannelsService externalChannelsService;


    @Override
    public Mono<ResponseEntity<Void>> sendPaperEngageRequest(String requestIdx, String xPagopaExtchCxId,
                                                             Mono<PaperEngageRequest> paperEngageRequest,
                                                             final ServerWebExchange exchange) {

        return paperEngageRequest
                .doOnNext(request -> log.info("Received request with requestBody: {}, headers: {}", request, exchange.getRequest().getHeaders()))
                .doOnNext(request -> externalChannelsService.sendPaperEngageRequest(request, NotificationProgress.PROGRESS_OUTPUT_CHANNEL.QUEUE_PAPER_CHANNEL))
                .map(notificationRequest -> Mono.just(ResponseEntity.noContent().build()))
                .log(this.getClass().getName())
                .onErrorResume(Mono::error).then(Mono.just(ResponseEntity.noContent().build()));

    }

    @Override
    public Mono<ResponseEntity<Void>> sendDigitalLegalMessage(String requestIdx, String xPagopaExtchCxId,
                                                              Mono<DigitalNotificationRequest> digitalNotificationRequest,
                                                              final ServerWebExchange exchange) {

        String appSourceName = getAppSourceName(exchange);

        return digitalNotificationRequest
                .doOnNext(request -> externalChannelsService.sendDigitalLegalMessage(request, appSourceName))
                .map(notificationRequest -> Mono.just(ResponseEntity.noContent().build()))
                .log(this.getClass().getName())
                .onErrorResume(Mono::error).then(Mono.just(ResponseEntity.noContent().build()));
    }

    @Override
    public Mono<ResponseEntity<Void>> sendDigitalCourtesyMessage(String requestIdx, String xPagopaExtchCxId,
                                                                 Mono<DigitalCourtesyMailRequest> digitalCourtesyMailRequest,
                                                                 final ServerWebExchange exchange) {

        String appSourceName = getAppSourceName(exchange);

        return digitalCourtesyMailRequest
                .doOnNext(request -> externalChannelsService.sendDigitalCourtesyMessage(request, appSourceName))
                .map(notificationRequest -> Mono.just(ResponseEntity.noContent().build()))
                .log(this.getClass().getName())
                .onErrorResume(Mono::error).then(Mono.just(ResponseEntity.noContent().build()));
    }

    @Override
    public Mono<ResponseEntity<Void>> sendCourtesyShortMessage(String requestIdx, String xPagopaExtchCxId,
                                                               Mono<DigitalCourtesySmsRequest> digitalCourtesySmsRequest,
                                                               final ServerWebExchange exchange) {

        String appSourceName = getAppSourceName(exchange);

        return digitalCourtesySmsRequest
                .doOnNext(request -> externalChannelsService.sendCourtesyShortMessage(request, appSourceName))
                .map(notificationRequest -> Mono.just(ResponseEntity.noContent().build()))
                .log()
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

