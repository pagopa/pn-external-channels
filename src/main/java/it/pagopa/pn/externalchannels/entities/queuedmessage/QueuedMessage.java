package it.pagopa.pn.externalchannels.entities.queuedmessage;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.cassandra.core.mapping.PrimaryKey;
import org.springframework.data.cassandra.core.mapping.Table;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.MongoId;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@Table(value = "queued_message")
@Document(value = "queued_message")
public class QueuedMessage {

    @PrimaryKey
    @MongoId
    private String id;

    private String eventId;

    private String eventStatus;

    private String requestCorrelationId;

    private String actCode;

    private String iun;

    private String communicationType;

    private String serviceLevel;

    private String printModel;

    private String template;

    // sender

    private String senderId;

    private String senderDenomination;

    private String senderPecAddress;

    // recipient

    private String recipientDenomination;

    private String recipientTaxId;

    private String pecAddress;

    private String at;

    private String address;

    private String addressDetails;

    private String zip;

    private String municipality;

    private String province;

    // elaboration result

    private List<String> attachmentKeys;

}
