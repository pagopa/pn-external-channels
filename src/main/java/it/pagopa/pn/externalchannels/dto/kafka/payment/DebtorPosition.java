package it.pagopa.pn.externalchannels.dto.kafka.payment;

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
