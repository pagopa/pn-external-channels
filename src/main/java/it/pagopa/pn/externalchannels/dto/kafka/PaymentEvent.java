package it.pagopa.pn.externalchannels.dto.kafka;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class PaymentEvent {
    private String id;
    private long timestamp;
    private DebtorPosition debtorPosition;
    private Creditor creditor;
    private PaymentInfo paymentInfo;
}
