package it.pagopa.pn.externalchannels.dto;

import it.pagopa.pn.externalchannels.model.DigitalCourtesyMailRequest;
import it.pagopa.pn.externalchannels.model.DigitalCourtesySmsRequest;
import it.pagopa.pn.externalchannels.model.DigitalNotificationRequest;
import it.pagopa.pn.externalchannels.model.PaperEngageRequest;
import lombok.*;

import java.time.Instant;

@Data
@Builder
public class ReceivedMessage {

    private String requestId;
    private String iun;
    private String recipientIndex;
    private Instant created;

    private DigitalNotificationRequest digitalNotificationRequest;
    private DigitalCourtesySmsRequest digitalCourtesySmsRequest;
    private DigitalCourtesyMailRequest digitalCourtesyMailRequest;
    private PaperEngageRequest paperEngageRequest;

}
