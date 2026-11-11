package it.pagopa.pn.externalchannels.dto.kafka.onboarding;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

import java.time.Instant;

@Data
public class OnboardingSelfCareMessage {

    private Billing billing;
    private String contentType;
    private String fileName;
    private String filePath;
    private String id;

    @Valid
    private Institution institution;

    private String internalInstitutionId;
    private String onboardingTokenId;
    private String product;
    private String state;
    private Instant createdAt;
    private Instant updatedAt;
    private String pricingPlan;
    private String zipCode;

    @Data
    public static class Billing {
        private String recipientCode;
        private String vatNumber;
        private Boolean publicServices;
    }

    @Data
    public static class Institution {
        private String address;
        private String description;
        private String digitalAddress;
        private String institutionType;
        private String origin;
        private String originId;

        @Pattern(regexp = "^(([A-Z]{6}[0-9LMNPQRSTUV]{2}[A-Z][0-9LMNPQRSTUV]{2}[A-Z][0-9LMNPQRSTUV]{3}[A-Z])|(\\d{11}))$")
        @NotNull
        private String taxCode;

        private RootParent rootParent;

        @Data
        public static class RootParent {
            private String id;
            private String originId;
            private String description;
        }
    }
}

