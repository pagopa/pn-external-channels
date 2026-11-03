package it.pagopa.pn.externalchannels.dto.kafka;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class PaymentInfo {
    private String paymentDateTime;
    private String dueDate;
    private String totalNotice;
    private String touchpoint;
    private String paymentChannel;
    private String amount;
    private String fee;
}
