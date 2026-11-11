package it.pagopa.pn.externalchannels.middleware;

import it.pagopa.pn.externalchannels.config.PnExternalChannelsProperties;
import it.pagopa.pn.externalchannels.dto.kafka.payment.Creditor;
import it.pagopa.pn.externalchannels.dto.kafka.payment.DebtorPosition;
import it.pagopa.pn.externalchannels.dto.kafka.payment.PaymentEvent;
import it.pagopa.pn.externalchannels.dto.kafka.payment.PaymentInfo;
import it.pagopa.pn.externalchannels.exception.KafkaUnavailableException;
import it.pagopa.pn.externalchannels.middleware.kafka.KafkaKillSwitch;
import it.pagopa.pn.externalchannels.mock_nodo_pagopa.KafkaProducerResponse;
import it.pagopa.pn.externalchannels.mock_nodo_pagopa.NoticePaymentRequest;
import lombok.CustomLog;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.Instant;

@Service
@CustomLog
@RequiredArgsConstructor
public class PaymentEventProducer {

    private final KafkaTemplate<String, PaymentEvent> kafkaTemplate;
    private final KafkaKillSwitch killSwitch;
    private final PnExternalChannelsProperties properties;

    public Mono<KafkaProducerResponse> send(NoticePaymentRequest request) {
        PaymentEvent event = toPaymentEvent(request);
        if(!killSwitch.isEnabled()) {
            log.debug("Kafka connection is disabled");
            return Mono.error(new KafkaUnavailableException());
        }
        return Mono.defer(() -> Mono.fromFuture(
                        kafkaTemplate.send(
                                properties.getKafkaTopics().getPayments(),
                                event.getId(),
                                event
                        )
                ))
                .doOnSuccess(result ->
                        log.info(
                                "Payment event published. PaymentId={}, Partition={}, Offset={}",
                                event.getId(),
                                result.getRecordMetadata().partition(),
                                result.getRecordMetadata().offset()
                        )
                )
                .onErrorMap(this::isKafkaConnectionError, error -> {
                    log.error(
                            "Kafka connection error. PaymentId={}",
                            event.getId(),
                            error
                    );

                    killSwitch.disable();

                    return new KafkaUnavailableException(error);
                })
                .map(result -> {
                    KafkaProducerResponse response = new KafkaProducerResponse();
                    response.setOffset(String.valueOf(result.getRecordMetadata().offset()));
                    response.setPartition(String.valueOf(result.getRecordMetadata().partition()));
                    return response;
                });
    }

    private PaymentEvent toPaymentEvent(NoticePaymentRequest request) {
        long timestamp = Instant.now().getEpochSecond();
        return PaymentEvent.builder()
                .id("evtAbcd"+timestamp)
                .timestamp(timestamp)
                .creditor(Creditor.builder()
                        .companyName("Example Company")
                        .idPA(request.getCreditorTaxId())
                        .build())
                .debtorPosition(DebtorPosition.builder()
                        .modelType("1")
                        .noticeNumber(request.getNoticeNumber())
                        .iur("iurAbcd"+timestamp)
                        .iuv("iuvAbcd"+timestamp)
                        .build())
                .paymentInfo(PaymentInfo.builder()
                        .amount(request.getAmount() != null ? request.getAmount() : "1000")
                        .fee("0")
                        .dueDate("2027-01-01")
                        .paymentDateTime(request.getPaymentDateTime())
                        .totalNotice("1")
                        .touchpoint("Example Touchpoint")
                        .paymentChannel("IO")
                        .build())
                .build();
    }

    private boolean isKafkaConnectionError(Throwable t) {
        // risale la catena delle cause: l'errore può arrivare incapsulato
        // (es. KafkaProducerException o ExecutionException) attorno al TimeoutException
        for (Throwable c = t; c != null; c = c.getCause()) {
            if (c instanceof org.apache.kafka.common.errors.TimeoutException
                    || c instanceof org.apache.kafka.common.errors.DisconnectException
                    || c instanceof org.apache.kafka.common.KafkaException && c.getCause() instanceof java.net.UnknownHostException) {
                return true;
            }
            if (c == c.getCause()) break;
        }
        return false;
    }
}