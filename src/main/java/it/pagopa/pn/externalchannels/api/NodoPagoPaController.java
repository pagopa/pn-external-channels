package it.pagopa.pn.externalchannels.api;

import it.pagopa.pn.externalchannels.middleware.PaymentEventProducer;
import it.pagopa.pn.externalchannels.mock_nodo_pagopa.NoticePaymentRequest;
import it.pagopa.pn.externalchannels.mock_nodo_pagopa.NoticePaymentResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@RestController
@RequiredArgsConstructor
public class NodoPagoPaController implements PaymentApi {
    private final PaymentEventProducer paymentEventProducer;
    public Mono<ResponseEntity<NoticePaymentResponse>> producePayment(Mono<NoticePaymentRequest> noticePaymentRequest, final ServerWebExchange exchange) {
        return noticePaymentRequest.flatMap(paymentEventProducer::send).map(ResponseEntity::ok);
    }
}
