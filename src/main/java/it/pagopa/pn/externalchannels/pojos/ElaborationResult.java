package it.pagopa.pn.externalchannels.pojos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ElaborationResult {

    private String recordId;

    private String digitalCommunicationCode;

    private String userProgressiveNumber;

    private String protocolNumber;

    private String receiverDenomination;

    private String receiverResidencePostalCode;

    private String receiverResidenceCity;

    private String receiverResidenceProvince;

    private String receiverResidenceAddress;

    private String plateOrTransportLine;

    private String originFileName;

    private String notificationAttemptNumber;

    private String emlWeight;

    private String pecAddress;

    private String senderPecAddress;

    private String messageId;

    private String resultType;

    private String resultDate;

    private String result;

    private String resultDenomination;

    private String iun;

}
