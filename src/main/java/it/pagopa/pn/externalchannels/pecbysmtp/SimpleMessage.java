package it.pagopa.pn.externalchannels.pecbysmtp;

import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Builder(toBuilder = true)
@ToString
public class SimpleMessage {

    private String eventId;
    private String iun;
    private String senderAddress;
    private String recipientAddress;
    private String subject;
    private String contentType;
    private String content;
    private String key;

}
