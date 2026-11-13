package it.pagopa.pn.externalchannels.dto.kafka.payment;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class PaymentEvent {
    private String id;
    private long timestamp;
    private String modelType;
    private String iuv;
    private String iur;
    private String noticeNumber;
    private String idPA;
    private String companyName;
    private String paymentDateTime;
    private String dueDate;
    private String totalNotice;
    private String touchpoint;
    private String paymentChannel;
    private String amount;
    private String fee;
}
