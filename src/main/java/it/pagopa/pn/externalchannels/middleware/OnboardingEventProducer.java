package it.pagopa.pn.externalchannels.middleware;

import it.pagopa.pn.externalchannels.config.PnExternalChannelsProperties;
import it.pagopa.pn.externalchannels.dto.kafka.onboarding.OnboardingSelfCareMessage;
import it.pagopa.pn.externalchannels.exception.KafkaUnavailableException;
import it.pagopa.pn.externalchannels.middleware.kafka.KafkaKillSwitch;
import it.pagopa.pn.externalchannels.mock_nodo_pagopa.KafkaProducerResponse;
import it.pagopa.pn.externalchannels.mock_nodo_pagopa.OnboardingBilling;
import it.pagopa.pn.externalchannels.mock_nodo_pagopa.OnboardingInstitution;
import it.pagopa.pn.externalchannels.mock_nodo_pagopa.OnboardingRequest;
import it.pagopa.pn.externalchannels.mock_nodo_pagopa.OnboardingRootParent;
import lombok.CustomLog;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.Date;

@Service
@CustomLog
@RequiredArgsConstructor
public class OnboardingEventProducer {

    private final KafkaTemplate<String, OnboardingSelfCareMessage> kafkaTemplate;
    private final KafkaKillSwitch killSwitch;
    private final PnExternalChannelsProperties properties;

    public Mono<KafkaProducerResponse> send(OnboardingRequest request) {
        OnboardingSelfCareMessage event = toOnboardingEvent(request);

        if (!killSwitch.isEnabled()) {
            log.debug("Kafka connection is disabled");
            return Mono.error(new KafkaUnavailableException());
        }

        return Mono.defer(() -> Mono.fromFuture(
                        kafkaTemplate.send(
                                properties.getKafkaTopics().getOnboardings(),
                                event.getId(),
                                event
                        )
                ))
                .doOnSuccess(result ->
                        log.info(
                                "Onboarding event published. OnboardingId={}, Partition={}, Offset={}",
                                event.getId(),
                                result.getRecordMetadata().partition(),
                                result.getRecordMetadata().offset()
                        )
                )
                .onErrorMap(this::isKafkaConnectionError, error -> {
                    log.error(
                            "Kafka connection error. OnboardingId={}",
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

    private OnboardingSelfCareMessage toOnboardingEvent(OnboardingRequest request) {
        String timestamp = String.valueOf(Instant.now().toEpochMilli());
        OnboardingSelfCareMessage event = new OnboardingSelfCareMessage();

        event.setId(request.getId() != null ? request.getId() : "onboarding-" + timestamp);
        event.setContentType(request.getContentType() != null ? request.getContentType() : "application/pdf");
        event.setFileName(request.getFileName() != null ? request.getFileName() : "onboarding.pdf");
        event.setFilePath(request.getFilePath() != null ? request.getFilePath() : "/tmp/onboarding.pdf");
        event.setProduct(request.getProduct() != null ? request.getProduct() : "prod-pn");
        event.setState(request.getState() != null ? request.getState() : "ACTIVE");
        event.setPricingPlan(request.getPricingPlan() != null ? request.getPricingPlan() : "free");
        event.setZipCode(request.getZipCode() != null ? request.getZipCode() : "00100");
        event.setCreatedAt(request.getCreatedAt() != null ? request.getCreatedAt().toInstant() : Instant.now());
        event.setUpdatedAt(request.getUpdatedAt() != null ? request.getUpdatedAt().toInstant() : Instant.now());

        if (request.getBilling() != null) {
            OnboardingBilling billing = request.getBilling();
            OnboardingSelfCareMessage.Billing mappedBilling = new OnboardingSelfCareMessage.Billing();
            mappedBilling.setRecipientCode(billing.getRecipientCode());
            mappedBilling.setVatNumber(billing.getVatNumber());
            mappedBilling.setPublicServices(billing.getPublicServices() != null ? billing.getPublicServices() : Boolean.TRUE);
            event.setBilling(mappedBilling);
        } else {
            OnboardingSelfCareMessage.Billing defaultBilling = new OnboardingSelfCareMessage.Billing();
            defaultBilling.setRecipientCode("RECIPIENT-001");
            defaultBilling.setVatNumber("12345678901");
            defaultBilling.setPublicServices(Boolean.TRUE);
            event.setBilling(defaultBilling);
        }

        OnboardingInstitution requestInstitution = request.getInstitution();
        if (requestInstitution != null) {
            OnboardingSelfCareMessage.Institution institution = new OnboardingSelfCareMessage.Institution();
            institution.setAddress(requestInstitution.getAddress());
            institution.setDescription(requestInstitution.getDescription());
            institution.setDigitalAddress(requestInstitution.getDigitalAddress());
            institution.setInstitutionType(requestInstitution.getInstitutionType() != null ? requestInstitution.getInstitutionType() : "PA");
            institution.setOrigin(requestInstitution.getOrigin() != null ? requestInstitution.getOrigin() : "IPA");
            institution.setOriginId(requestInstitution.getOriginId());
            institution.setTaxCode(requestInstitution.getTaxCode() != null ? requestInstitution.getTaxCode() : "12345678901");

            OnboardingRootParent rootParent = requestInstitution.getRootParent();
            if (rootParent != null) {
                OnboardingSelfCareMessage.Institution.RootParent mappedRootParent = new OnboardingSelfCareMessage.Institution.RootParent();
                mappedRootParent.setId(rootParent.getId());
                mappedRootParent.setOriginId(rootParent.getOriginId());
                mappedRootParent.setDescription(rootParent.getDescription());
                institution.setRootParent(mappedRootParent);
            }
            event.setInstitution(institution);
        } else {
            OnboardingSelfCareMessage.Institution institution = new OnboardingSelfCareMessage.Institution();
            institution.setInstitutionType("PA");
            institution.setOrigin("IPA");
            institution.setTaxCode("12345678901");
            event.setInstitution(institution);
        }

        event.setInternalInstitutionId(request.getInternalInstitutionId() != null ? request.getInternalInstitutionId() : "int-inst-" + timestamp);
        event.setOnboardingTokenId(request.getOnboardingTokenId() != null ? request.getOnboardingTokenId() : "token-" + timestamp);

        return event;
    }

    private boolean isKafkaConnectionError(Throwable t) {
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

