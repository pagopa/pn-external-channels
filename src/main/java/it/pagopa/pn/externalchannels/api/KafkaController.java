package it.pagopa.pn.externalchannels.api;

import it.pagopa.pn.externalchannels.middleware.OnboardingEventProducer;
import it.pagopa.pn.externalchannels.middleware.PaymentEventProducer;
import it.pagopa.pn.externalchannels.middleware.kafka.KafkaKillSwitch;
import it.pagopa.pn.externalchannels.mock_nodo_pagopa.KafkaConnection;
import it.pagopa.pn.externalchannels.mock_nodo_pagopa.KafkaProducerResponse;
import it.pagopa.pn.externalchannels.mock_nodo_pagopa.NoticePaymentRequest;
import it.pagopa.pn.externalchannels.mock_nodo_pagopa.OnboardingRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@RestController
@RequiredArgsConstructor
public class KafkaController implements ConnectionApi, PaymentApi, OnboardingApi {
    private final PaymentEventProducer paymentEventProducer;
    private final OnboardingEventProducer onboardingEventProducer;
    private final KafkaKillSwitch killSwitch;

    @Override
    public Mono<ResponseEntity<KafkaConnection>> checkKafkaConnection(final ServerWebExchange exchange) {
        return Mono.just(ResponseEntity.ok(new KafkaConnection(killSwitch.isEnabled())));
    }

    @Override
    public Mono<ResponseEntity<KafkaConnection>> setKafkaConnection(Mono<KafkaConnection> kafkaConnection,  final ServerWebExchange exchange) {
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

    @Override
    public Mono<ResponseEntity<KafkaProducerResponse>> producePayment(Mono<NoticePaymentRequest> noticePaymentRequest, final ServerWebExchange exchange) {
        return noticePaymentRequest.flatMap(paymentEventProducer::send).map(ResponseEntity::ok);
    }

    @Override
    public Mono<ResponseEntity<KafkaProducerResponse>> produceOnboarding(Mono<OnboardingRequest> onboardingRequest, final ServerWebExchange exchange) {
        return onboardingRequest.flatMap(onboardingEventProducer::send).map(ResponseEntity::ok);
    }
}
