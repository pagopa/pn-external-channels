package it.pagopa.pn.externalchannels.api;

import it.pagopa.pn.externalchannels.middleware.PaymentEventProducer;
import it.pagopa.pn.externalchannels.middleware.kafka.KafkaKillSwitch;
import it.pagopa.pn.externalchannels.mock_nodo_pagopa.KafkaConnection;
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
    private final KafkaKillSwitch killSwitch;

    public Mono<ResponseEntity<KafkaConnection>> checkNodoPagopaConnection(final ServerWebExchange exchange) {
        return Mono.just(ResponseEntity.ok(new KafkaConnection(killSwitch.isEnabled())));
    }

    public Mono<ResponseEntity<KafkaConnection>> setNodoPagopaConnection(Mono<KafkaConnection> kafkaConnection,  final ServerWebExchange exchange) {
        return kafkaConnection
                .map(connection -> {
                    if (connection.getEnabled()) {
                        killSwitch.enable();
                    } else {
                        killSwitch.disable();
                    }
                    return ResponseEntity.ok(new KafkaConnection(killSwitch.isEnabled()));
                });
    }

    public Mono<ResponseEntity<NoticePaymentResponse>> producePayment(Mono<NoticePaymentRequest> noticePaymentRequest, final ServerWebExchange exchange) {
        return noticePaymentRequest.flatMap(paymentEventProducer::send).map(ResponseEntity::ok);
    }
}
