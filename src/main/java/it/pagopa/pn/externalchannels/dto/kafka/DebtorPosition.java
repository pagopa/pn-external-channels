package it.pagopa.pn.externalchannels.dto.kafka;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class DebtorPosition {
    private String modelType;
    private String iuv;
    private String iur;
    private String noticeNumber;
}
