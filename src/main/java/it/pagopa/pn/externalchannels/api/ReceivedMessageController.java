package it.pagopa.pn.externalchannels.api;

import it.pagopa.pn.externalchannels.mockreceivedmessage.ReceivedMessage;
import it.pagopa.pn.externalchannels.service.ReceivedMessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;


@RestController
@RequiredArgsConstructor
public class ReceivedMessageController implements MockReceivedMessagesApi {

    private final ReceivedMessageService service;

    @Override
    public Mono<ResponseEntity<ReceivedMessage>> getReceivedMessage(String requestId, ServerWebExchange exchange) {
        return service.findByRequestId(requestId)
                .map(ResponseEntity::ok)
                .switchIfEmpty(Mono.just(ResponseEntity.notFound().build()));
    }

    @Override
    public Mono<ResponseEntity<Flux<ReceivedMessage>>> getReceivedMessages(String iun, Integer recipientIndex, ServerWebExchange exchange) {
        return Mono.fromSupplier(() -> ResponseEntity.ok(service.findByIunRecIndex(iun, recipientIndex)));
    }
}
