package it.pagopa.pn.externalchannels.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class OcrOutputMessage {
    private String version;
    private String commandId;
    private String commandType;
    private DataField data;

    @lombok.Data
    @lombok.Builder
    public static class DataField {
        private ValidationType validationType;
        private ValidationStatus validationStatus;
        private String description;
    }

    public enum ValidationType {
        operator, ai
    }

    public enum ValidationStatus {
        PENDING, KO
    }
}